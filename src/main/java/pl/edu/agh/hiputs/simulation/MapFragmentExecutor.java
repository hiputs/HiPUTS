package pl.edu.agh.hiputs.simulation;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.loadbalancer.LoadBalancingService;
import pl.edu.agh.hiputs.loadbalancer.LocalLoadMonitorService;
import pl.edu.agh.hiputs.loadbalancer.utils.CarCounterUtil;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.CarGeneratorService;
import pl.edu.agh.hiputs.service.worker.usecase.CarSynchronizationService;
import pl.edu.agh.hiputs.service.worker.usecase.CarsOnBorderSynchronizationService;
import pl.edu.agh.hiputs.service.worker.usecase.PatchTransferService;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;
import pl.edu.agh.hiputs.statistics.SimulationPoint;
import pl.edu.agh.hiputs.statistics.worker.IterationStatisticsService;
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
  private final LocalLoadMonitorService localLoadMonitorService;
  private final LoadBalancingService loadBalancingService;
  private final MessageSenderService messageSenderService;

  private final PatchTransferService patchTransferService;
  private final CarGeneratorService carGeneratorService;
  private final SimulationStatisticService simulationStatisticService;
  private final IterationStatisticsService iterationStatisticsService;

  public void run(int step) {
    try {
      iterationStatisticsService.startSimulationStep();

      // 3. decision
      log.info("Step 3 start");
      iterationStatisticsService.startStage(
          List.of(SimulationPoint.FULL_STEP, SimulationPoint.DECISION_STAGE, SimulationPoint.FIRST_ITERATION));
      List<Runnable> decisionStageTasks = mapFragment.getLocalLaneIds()
          .stream()
          .map(laneId -> new LaneDecisionStageTask(mapFragment, laneId, carGeneratorService,
              ConfigurationService.getConfiguration().isReplaceCarWithFinishedRoute()))
          .collect(Collectors.toList());
      taskExecutor.executeBatch(decisionStageTasks);

      iterationStatisticsService.endStage(SimulationPoint.DECISION_STAGE);

      // 4. send incoming sets of cars to neighbours
      log.info("Step 4 start");
      iterationStatisticsService.startStage(SimulationPoint.SENDING_CARS);
      int sendCars = carSynchronizationService.sendIncomingSetsOfCarsToNeighbours(mapFragment);
      iterationStatisticsService.endStage(List.of(SimulationPoint.FIRST_ITERATION, SimulationPoint.SENDING_CARS));

      // 5. receive incoming sets of cars from neighbours
      log.info("Step 5 start");
      iterationStatisticsService.startStage(SimulationPoint.WAITING_RECEIVING_CARS);
      carSynchronizationService.synchronizedGetIncomingSetsOfCars(mapFragment);
      iterationStatisticsService.endStage(SimulationPoint.WAITING_RECEIVING_CARS);

      // 6. 7. insert incoming cars & update lanes/cars
      log.info("Step 6,7 start");
      iterationStatisticsService.startStage(SimulationPoint.SECOND_ITERATION_UPDATING_CARS);
      List<Runnable> updateStageTasks = mapFragment.getLocalLaneIds().stream()
          .map(laneId -> new LaneUpdateStageTask(mapFragment, laneId))
          .collect(Collectors.toList());
      taskExecutor.executeBatch(updateStageTasks);
      iterationStatisticsService.endStage(SimulationPoint.SECOND_ITERATION_UPDATING_CARS);

      iterationStatisticsService.startStage(SimulationPoint.SECOND_ITERATION_NOTIFY);
      Triple<Integer, Integer, Double> carsStats = CarCounterUtil.countAllCarStats(mapFragment);
      iterationStatisticsService.setCarsNumberInStep(carsStats.getLeft());
      iterationStatisticsService.setStoppedCars(carsStats.getMiddle());
      iterationStatisticsService.setSpeedSum(carsStats.getRight());
      localLoadMonitorService.notifyAboutMyLoad(step);
      iterationStatisticsService.endStage(SimulationPoint.SECOND_ITERATION_NOTIFY);

      // 8. load balancing
      log.info("Step 8 start");
      iterationStatisticsService.startStage(
          List.of(SimulationPoint.LOAD_BALANCING, SimulationPoint.LOAD_BALANCING_START));
      MapFragmentId lastLoadBalancingCandidate = loadBalancingService.startLoadBalancing(mapFragment);
      iterationStatisticsService.endStage(SimulationPoint.LOAD_BALANCING_START);

      log.info("Step 8 - 1 start");
      iterationStatisticsService.startStage(SimulationPoint.LOAD_BALANCING_NOTIFICATIONS);
      patchTransferService.retransmitNotification(lastLoadBalancingCandidate, mapFragment);
      iterationStatisticsService.startStage(SimulationPoint.WAITING_RECEIVING_RETRANSMISSIONS);
      patchTransferService.synchronizedGetRetransmittedNotification(mapFragment);
      iterationStatisticsService.endStage(SimulationPoint.WAITING_RECEIVING_RETRANSMISSIONS);
      patchTransferService.handleReceivedPatch(mapFragment);
      patchTransferService.handleNotificationPatch(mapFragment);

      iterationStatisticsService.endStage(
          List.of(SimulationPoint.LOAD_BALANCING_NOTIFICATIONS, SimulationPoint.LOAD_BALANCING));

      // 9. send and receive remote patches (border patches)
      log.info("Step 9 start");
      iterationStatisticsService.startStage(SimulationPoint.SYNCHRONIZATION_AREA_SEND_PATCHES);
      List<Pair<String, Integer>> borderCars =
          carsOnBorderSynchronizationService.sendCarsOnBorderToNeighbours(mapFragment);
      iterationStatisticsService.endStage(SimulationPoint.SYNCHRONIZATION_AREA_SEND_PATCHES);
      log.info("Step 9 - 1 start");
      iterationStatisticsService.startStage(SimulationPoint.WAITING_RECEIVING_PATCHES);
      carsOnBorderSynchronizationService.synchronizedGetRemoteCars(mapFragment);
      iterationStatisticsService.endStage(SimulationPoint.WAITING_RECEIVING_PATCHES);

      // 10. save statistic
      log.info("Step 10 start");
      // if (step % 10 == 0) {
        simulationStatisticService.saveMapStatistic(mapFragment.getMapStatistic(step));
      // }

      // 11. gen new car
      log.info("Step 11 start");

      iterationStatisticsService.startStage(SimulationPoint.MANAGE_CARS);
      carGeneratorService.manageCars(step, mapFragment);
      iterationStatisticsService.endStage(SimulationPoint.MANAGE_CARS);

      // mapFragment.printFullStatistic();

      log.debug("Worker: {}, ShadowPatches: {}", mapFragment.getMe().getId(), mapFragment.getShadowPatchesReadable()
          .stream()
          .map(patch -> patch.getPatchId().getValue())
          .collect(Collectors.joining(",")));
      log.debug("Worker: {}, LocalPatches: {}", mapFragment.getMe().getId(), mapFragment.getLocalPatchesEditable()
          .stream()
          .map(patch -> patch.getPatchId().getValue())
          .collect(Collectors.joining(",")));
      log.debug("Worker: {}, BorderPatches: {}", mapFragment.getMe().getId(), mapFragment.getBorderPatches()
          .entrySet()
          .stream()
          .map(entry -> entry.getKey().getId() + entry.getValue()
              .stream()
              .map(patch -> patch.getPatchId().getValue())
              .collect(Collectors.joining(",")))
          .collect(Collectors.joining(",")));
      iterationStatisticsService.endStage(SimulationPoint.FULL_STEP);
      iterationStatisticsService.setOutgoingMessagesInStep(messageSenderService.getSentMessages());
      iterationStatisticsService.setOutgoingMessagesToServerInStep(messageSenderService.getSentServerMessages());
      iterationStatisticsService.setOutgoingMessagesSize(messageSenderService.getSentMessagesSize());
      String borderCarsInfo = borderCars.stream()
          .map(pair -> pair.getLeft() + ":" + pair.getRight().toString() + ",")
          .collect(Collectors.joining());
      iterationStatisticsService.setInfo(sendCars + "," + borderCarsInfo);
      iterationStatisticsService.setMemoryUsage();
      iterationStatisticsService.endSimulationStep();

    } catch (Exception e) {
      log.error("Unexpected exception occurred", e);
    }
  }
}
