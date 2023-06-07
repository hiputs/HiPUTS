package pl.edu.agh.hiputs.simulation;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.loadbalancer.LoadBalancingService;
import pl.edu.agh.hiputs.loadbalancer.MonitorLocalService;
import pl.edu.agh.hiputs.loadbalancer.model.SimulationPoint;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.service.routegenerator.CarGeneratorService;
import pl.edu.agh.hiputs.service.routegenerator.FileGeneratorService;
import pl.edu.agh.hiputs.service.routegenerator.RandomCarGeneratorService;
import pl.edu.agh.hiputs.service.worker.usecase.CarsOnBorderSynchronizationService;
import pl.edu.agh.hiputs.service.worker.usecase.CarSynchronizationService;
import pl.edu.agh.hiputs.service.worker.usecase.PatchTransferService;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;
import pl.edu.agh.hiputs.tasks.LaneDecisionStageTask;
import pl.edu.agh.hiputs.tasks.LaneUpdateStageTask;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapFragmentExecutor {

  @Setter
  @Getter
  private MapFragment mapFragment;
  private final TaskExecutorService taskExecutor;
  private final CarSynchronizationService carSynchronizationService;
  private final CarsOnBorderSynchronizationService carsOnBorderSynchronizationService;
  private final MonitorLocalService monitorLocalService;
  private final LoadBalancingService loadBalancingService;
  private final PatchTransferService patchTransferService;
  private final CarGeneratorService carGeneratorService;
  private final FileGeneratorService fileGeneratorService;
  private final SimulationStatisticService simulationStatisticService;

  public void run(int step) {
//    mapFragment.localPatches().forEach(patch -> {
//      System.out.println("PATCH " + patch.getPatchId().getValue());
//      patch.lanes.values().forEach(lane -> {
//        System.out.println("LANEID " + lane.getLaneId());
//        System.out.println("IN " + lane.getIncomingJunctionId());
//        System.out.println("OUT " + lane.getOutgoingJunctionId());
//      });
//    });


    try {
      // 3. decision
      log.debug("Step 3 start");
      monitorLocalService.startSimulationStep();
      List<Runnable> decisionStageTasks = mapFragment.getLocalLaneIds()
        .parallelStream()
        .map(laneId -> new LaneDecisionStageTask(mapFragment, laneId))
        .collect(Collectors.toList());
      taskExecutor.executeBatch(decisionStageTasks);

      // 4. send incoming sets of cars to neighbours
      log.debug("Step 4 start");
      carSynchronizationService.sendIncomingSetsOfCarsToNeighbours(mapFragment);

      // 5. receive incoming sets of cars from neighbours
      log.debug("Step 5 start");
      monitorLocalService.markPointAsFinish(SimulationPoint.FIRST_ITERATION);
      carSynchronizationService.synchronizedGetIncomingSetsOfCars(mapFragment);
      monitorLocalService.markPointAsFinish(SimulationPoint.WAITING_FOR_FIRST_ITERATION);

      // 6. 7. insert incoming cars & update lanes/cars
      log.debug("Step 6,7 start");
      List<Runnable> updateStageTasks = mapFragment.getLocalLaneIds()
        .parallelStream()
        .map(laneId -> new LaneUpdateStageTask(mapFragment, laneId))
        .collect(Collectors.toList());
      taskExecutor.executeBatch(updateStageTasks);
      monitorLocalService.markPointAsFinish(SimulationPoint.SECOND_ITERATION);
      monitorLocalService.notifyAboutMyLoad();

      // 8. load balancing
      log.debug("Step 8 start");
      MapFragmentId lastLoadBalancingCandidate = loadBalancingService.startLoadBalancing(mapFragment);

      patchTransferService.retransmitNotification(lastLoadBalancingCandidate);
      patchTransferService.handleReceivedPatch(mapFragment);
      patchTransferService.handleNotificationPatch(mapFragment);
      monitorLocalService.markPointAsFinish(SimulationPoint.LOAD_BALANCING);

      // 9. send and receive remote patches (border patches)
      log.debug("Step 9 start");
      carsOnBorderSynchronizationService.sendCarsOnBorderToNeighbours(mapFragment);
      monitorLocalService.markPointAsFinish(SimulationPoint.SYNCHRONIZATION_AREA);
      log.debug("Step 9 - 1 start");
      carsOnBorderSynchronizationService.synchronizedGetRemoteCars(mapFragment);

      monitorLocalService.markPointAsFinish(SimulationPoint.WAITING_FOR_SECOND_ITERATION);
      monitorLocalService.endSimulationStep();

      // 10. save statistic
      log.debug("Step 10 start");
      simulationStatisticService.saveMapStatistic(mapFragment.getMapStatistic(step));

      // 11. gen new car
      log.debug("Step 11 start");

//      TODO: osobny job na generowanie?
      if (step == 0)
        fileGeneratorService.generateFiles(mapFragment);

      carGeneratorService.generateCars(mapFragment, step);

      // mapFragment.printFullStatistic();

    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }
}
