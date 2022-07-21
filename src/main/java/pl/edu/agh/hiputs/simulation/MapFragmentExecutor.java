package pl.edu.agh.hiputs.simulation;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.service.worker.MessageReceiverService;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.service.worker.usecase.CarSynchronizedService;
import pl.edu.agh.hiputs.tasks.LaneDecisionStageTask;
import pl.edu.agh.hiputs.tasks.LaneUpdateStageTask;

@Service
@RequiredArgsConstructor
public class MapFragmentExecutor {

  @Setter
  @Getter
  private MapFragment mapFragment;
  private final MessageReceiverService messageReceiverService;
  private final SubscriptionService subscriptionService;
  private final TaskExecutorService taskExecutor;
  private final MessageSenderService messageSenderService;
  private final CarSynchronizedService carSynchronizedService;

  public void run() {

    // 3. decision
    List<Runnable> decisionStageTasks = mapFragment.getLocalLaneIds()
        .stream()
        .map(laneId -> new LaneDecisionStageTask(mapFragment, laneId))
        .collect(Collectors.toList());
    taskExecutor.executeBatch(decisionStageTasks);

    // 4. prepare messages
    carSynchronizedService.sendCarsToNeighbours(mapFragment);

    // 5. send & receive border patches
    carSynchronizedService.synchronizedGetIncomingCar(mapFragment);

    // 6. 7. insert incoming cars & update lanes/cars
    List<Runnable> updateStageTasks = mapFragment.getLocalLaneIds()
        .stream()
        .map(laneId -> new LaneUpdateStageTask(mapFragment, laneId))
        .collect(Collectors.toList());
    taskExecutor.executeBatch(updateStageTasks);

    // 8. send and receive remote patches (border patches)
    // todo: fill when HiPUTS#34 is completed
  }
}
