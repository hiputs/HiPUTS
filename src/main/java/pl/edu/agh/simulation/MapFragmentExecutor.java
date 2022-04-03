package pl.edu.agh.simulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.communication.service.MessageReceiverService;
import pl.edu.agh.communication.service.MessageSenderService;
import pl.edu.agh.communication.service.SubscriptionService;
import pl.edu.agh.model.actor.MapFragment;
import pl.edu.agh.model.map.example.ExampleMapFragmentProvider;
import pl.edu.agh.scheduler.SchedulerService;
import pl.edu.agh.scheduler.TaskExecutorService;
import pl.edu.agh.service.CarSynchronizedServiceImpl;
import pl.edu.agh.service.usecase.CarSynchronizedService;
import pl.edu.agh.tasks.LaneDecisionStageTask;
import pl.edu.agh.tasks.LaneUpdateStageTask;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MapFragmentExecutor {

    public MapFragment mf = ExampleMapFragmentProvider.getSimpleMap2();

    @Autowired
    TaskExecutorService taskExecutor;

    public void run() {
        MessageReceiverService messageReceiverService = new MessageReceiverService();
        SubscriptionService subscriptionService = new SubscriptionService(messageReceiverService);
        TaskExecutorService taskExecutorService = new SchedulerService();
        MessageSenderService messageSenderService = new MessageSenderService(subscriptionService);
        CarSynchronizedService carSynchronizedService = new CarSynchronizedServiceImpl(mf, subscriptionService, taskExecutorService, messageSenderService);

        // 3. decision
        List<Runnable> decisionStageTasks = mf.getAllManagedLaneIds().stream()
                .map(laneId -> new LaneDecisionStageTask(mf, laneId))
                .collect(Collectors.toList());
        taskExecutor.executeBatch(decisionStageTasks);

        // 4. prepare messages
        carSynchronizedService.sendCarsToNeighbours();
        // 5. send & receive border patches
        carSynchronizedService.synchronizedGetIncomingCar();

        // 6. 7. insert incoming cars & update lanes/cars
        List<Runnable> updateStageTasks = mf.getAllManagedLaneIds().stream()
                .map(laneId -> new LaneUpdateStageTask(mf, laneId))
                .collect(Collectors.toList());
        taskExecutor.executeBatch(updateStageTasks);

        // 8. send and receive remote patches (border patches)
        // todo: fill when HiPUTS#34 is completed
    }
}
