package pl.edu.agh.hiputs.simulation;

import java.util.Collection;
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
import pl.edu.agh.hiputs.configuration.Configuration;
import pl.edu.agh.hiputs.loadbalancer.LoadBalancingService;
import pl.edu.agh.hiputs.loadbalancer.LocalLoadMonitorService;
import pl.edu.agh.hiputs.loadbalancer.utils.CarCounterUtil;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.service.routegenerator.CarGeneratorService;
import pl.edu.agh.hiputs.service.worker.usecase.CarSynchronizationService;
import pl.edu.agh.hiputs.service.worker.usecase.CarsOnBorderSynchronizationService;
import pl.edu.agh.hiputs.service.worker.usecase.PatchTransferService;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;
import pl.edu.agh.hiputs.service.worker.usecase.TrafficLightsStrategyFactoryService;
import pl.edu.agh.hiputs.tasks.JunctionLightsUpdateStageTask;
import pl.edu.agh.hiputs.tasks.RoadDecisionStageTask;
import pl.edu.agh.hiputs.tasks.RoadUpdateStageTask;
import pl.edu.agh.hiputs.statistics.SimulationPoint;
import pl.edu.agh.hiputs.statistics.worker.IterationStatisticsService;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapFragmentExecutor {

  private final Configuration configuration;
  @Setter
  @Getter
  private Collection<JunctionId> junctionsWithTrafficLights;
  private final TrafficLightsStrategyFactoryService trafficLightsStrategyFactoryService;
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
  @Setter
  @Getter
  private MapFragment mapFragment;

  public void run(int step) {
    try {
      iterationStatisticsService.startSimulationStep();

      // 3. decision
      log.debug("Step 3 start");
      iterationStatisticsService.startStage(
          List.of(SimulationPoint.FULL_STEP, SimulationPoint.DECISION_STAGE, SimulationPoint.FIRST_ITERATION));
      List<Runnable> decisionStageTasks = mapFragment.getLocalRoadIds()
          .stream()
          .map(roadId -> new RoadDecisionStageTask(mapFragment, roadId, carGeneratorService,
              configuration.isReplaceCarWithFinishedRoute()))
          .collect(Collectors.toList());
      taskExecutor.executeBatch(decisionStageTasks);

      iterationStatisticsService.endStage(SimulationPoint.DECISION_STAGE);

      // 4. send incoming sets of cars to neighbours
      log.debug("Step 4 start");
      iterationStatisticsService.startStage(SimulationPoint.SENDING_CARS);
      int sendCars = carSynchronizationService.sendIncomingSetsOfCarsToNeighbours(mapFragment);
      iterationStatisticsService.endStage(List.of(SimulationPoint.FIRST_ITERATION, SimulationPoint.SENDING_CARS));

      // 5. receive incoming sets of cars from neighbours
      log.debug("Step 5 start");
      iterationStatisticsService.startStage(SimulationPoint.WAITING_RECEIVING_CARS);
      carSynchronizationService.synchronizedGetIncomingSetsOfCars(mapFragment);
      iterationStatisticsService.endStage(SimulationPoint.WAITING_RECEIVING_CARS);

      // 6. 7. insert incoming cars & update lanes/cars  & update traffic lights
      log.debug("Step 6,7 start");
      iterationStatisticsService.startStage(SimulationPoint.SECOND_ITERATION_UPDATING_CARS);
      List<Runnable> updateStageTasks = mapFragment.getLocalRoadIds().stream()
          .map(roadId -> new RoadUpdateStageTask(mapFragment, roadId))
          .collect(Collectors.toList());

      updateStageTasks.addAll(mapFragment.getLocalJunctionIds()
          .parallelStream()
          .map(junctionId -> new JunctionLightsUpdateStageTask(mapFragment, junctionId,
              trafficLightsStrategyFactoryService.getFromConfiguration()))
          .toList());

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
      log.debug("Step 8 start");
      iterationStatisticsService.startStage(
          List.of(SimulationPoint.LOAD_BALANCING, SimulationPoint.LOAD_BALANCING_START));
      MapFragmentId lastLoadBalancingCandidate = loadBalancingService.startLoadBalancing(mapFragment);
      iterationStatisticsService.endStage(SimulationPoint.LOAD_BALANCING_START);

      log.debug("Step 8 - 1 start");
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
      log.debug("Step 9 start");
      iterationStatisticsService.startStage(SimulationPoint.SYNCHRONIZATION_AREA_SEND_PATCHES);
      List<Pair<String, Integer>> borderCars =
          carsOnBorderSynchronizationService.sendCarsOnBorderToNeighbours(mapFragment);
      iterationStatisticsService.endStage(SimulationPoint.SYNCHRONIZATION_AREA_SEND_PATCHES);
      log.debug("Step 9 - 1 start");
      iterationStatisticsService.startStage(SimulationPoint.WAITING_RECEIVING_PATCHES);
      carsOnBorderSynchronizationService.synchronizedGetRemoteCars(mapFragment);
      iterationStatisticsService.endStage(SimulationPoint.WAITING_RECEIVING_PATCHES);

      // 10. save statistic
      log.debug("Step 10 start");
      if (step % 50 == 0) {
        simulationStatisticService.saveMapStatistic(mapFragment.getMapStatistic(step));
      }

      // 11. gen new car
      log.debug("Step 11 start");
      /**
       * TODO: Create separate main class for generating route files - Currently this `if` is a workaround. -> `if`
       * moved to CarGeneratorServiceImpl->generateInitialCars()
       * To achieve that `SingleWorkStrategyService` and `SingleWorkStrategyService` need the same source of data.
       * Currently `SingleWorkStrategyService` uses `MapFragment` directly with empty `MapRepository`
       * Possible fix:
       * - Create `SingleMapFragmentMapRepository` that would extend `MapRepository` and load directly from map fragment
       *     in memory (instead of files on disk)
       * - Conditionally create either `SingleMapFragmentMapRepository` or `MapRepositoryImpl` based on `testMode` flag
       * - Create separate main class for generating route files
       */
      iterationStatisticsService.startStage(SimulationPoint.MANAGE_CARS);
      carGeneratorService.manageCars(mapFragment, step);
      iterationStatisticsService.endStage(SimulationPoint.MANAGE_CARS);

      // mapFragment.printFullStatistic();
      logPatchesInfo();
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

  private void logPatchesInfo() {
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
  }
}
