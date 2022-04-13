package pl.edu.agh.hiputs.simulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.service.MessageReceiverService;
import pl.edu.agh.hiputs.communication.service.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.SubscriptionService;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.scheduler.SchedulerService;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.service.CarSynchronizedServiceImpl;
import pl.edu.agh.hiputs.service.usecase.CarSynchronizedService;
import pl.edu.agh.hiputs.tasks.LaneDecisionStageTask;
import pl.edu.agh.hiputs.tasks.LaneUpdateStageTask;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MapFragmentExecutor {

    public final MapFragment mapFragment = ExampleMapFragmentProvider.getSimpleMap2();

    private final TaskExecutorService taskExecutor;

    @Autowired
    public MapFragmentExecutor(TaskExecutorService taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void run() {
        MessageReceiverService messageReceiverService = new MessageReceiverService();
        SubscriptionService subscriptionService = new SubscriptionService(messageReceiverService);
        TaskExecutorService taskExecutorService = new SchedulerService();
        MessageSenderService messageSenderService = new MessageSenderService(subscriptionService);
        CarSynchronizedService carSynchronizedService = new CarSynchronizedServiceImpl(mapFragment, subscriptionService, taskExecutorService, messageSenderService);

        // 3. decision
        List<Runnable> decisionStageTasks = mapFragment.getLocalLaneIds().stream()
                .map(laneId -> new LaneDecisionStageTask(mapFragment, laneId))
                .collect(Collectors.toList());
        taskExecutor.executeBatch(decisionStageTasks);

        // 4. prepare messages
        carSynchronizedService.sendCarsToNeighbours();

        // 5. send & receive border patches
        carSynchronizedService.synchronizedGetIncomingCar();

        // 6. 7. insert incoming cars & update lanes/cars
        List<Runnable> updateStageTasks = mapFragment.getLocalLaneIds().stream()
                .map(laneId -> new LaneUpdateStageTask(mapFragment, laneId))
                .collect(Collectors.toList());
        taskExecutor.executeBatch(updateStageTasks);

        // 8. send and receive remote patches (border patches)
        // todo: fill when HiPUTS#34 is completed
    }
}
