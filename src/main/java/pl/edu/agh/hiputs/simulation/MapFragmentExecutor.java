package pl.edu.agh.hiputs.simulation;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.service.worker.MessageReceiverService;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.service.worker.usecase.CarsOnBorderSynchronizationService;
import pl.edu.agh.hiputs.service.worker.usecase.CarSynchronizationService;
import pl.edu.agh.hiputs.tasks.LaneDecisionStageTask;
import pl.edu.agh.hiputs.tasks.LaneUpdateStageTask;
import pl.edu.agh.hiputs.visualization.connection.VisualizationService;

@Slf4j
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
  private final VisualizationService visualizationService;
  private final CarSynchronizationService carSynchronizationService;
  private final CarsOnBorderSynchronizationService carsOnBorderSynchronizationService;

  public void run() {
    try {
      // 3. decision
      List<Runnable> decisionStageTasks = mapFragment.getLocalLaneIds()
          .stream()
          .map(laneId -> new LaneDecisionStageTask(mapFragment, laneId))
          .collect(Collectors.toList());
      taskExecutor.executeBatch(decisionStageTasks);
      visualizationService.sendCarsFromMapFragment(mapFragment);

      // 4. send incoming sets of cars to neighbours
      carSynchronizationService.sendIncomingSetsOfCarsToNeighbours(mapFragment);

      // 5. receive incoming sets of cars from neighbours
      carSynchronizationService.synchronizedGetIncomingSetsOfCars(mapFragment);

      // 6. 7. insert incoming cars & update lanes/cars
      List<Runnable> updateStageTasks = mapFragment.getLocalLaneIds()
          .stream()
          .map(laneId -> new LaneUpdateStageTask(mapFragment, laneId))
          .collect(Collectors.toList());
      taskExecutor.executeBatch(updateStageTasks);

      // 8. send and receive remote patches (border patches)
      carsOnBorderSynchronizationService.sendCarsOnBorderToNeighbours(mapFragment);
      carsOnBorderSynchronizationService.synchronizedGetRemoteCars(mapFragment);

    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }
}
