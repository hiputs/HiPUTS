package pl.edu.agh.hiputs.startingUp;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.CompletedInitializationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.FinishSimulationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.FinishSimulationStatisticMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.WorkerConnectionMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.MapReadyToReadMessage;
import pl.edu.agh.hiputs.communication.service.server.MessageSenderServerService;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.server.DivideService;
import pl.edu.agh.hiputs.service.server.WorkerSynchronisationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerStrategyService implements Strategy {

  private final WorkerSynchronisationService workerSynchronisationService;
  private final ConfigurationService configurationService;
  private final WorkerStrategyService workerStrategyService;
  private final DivideService divideService;
  private final ExecutorService workerPrepareExecutor = newSingleThreadExecutor();
  private final MessageSenderServerService messageSenderServerService;

  @Override
  public void executeStrategy() {

    log.info("Running server");
    workerPrepareExecutor.submit(new PrepareWorkerTask());

    List<Patch> patches = createPatches();
    Map<String, List<PatchId>> dividedPatchesIds = divideService.divide(patches);

    log.info("Start waiting for all workers be in state WorkerConnection");
    workerSynchronisationService.waitForAllWorkers(WorkerConnectionMessage);

    if (configurationService.getConfiguration().isParsedMap()) {
      messageSenderServerService.broadcast(new MapReadyToReadMessage());
    }

    log.info("Waiting for all workers by in state CompletedInitialization");
    workerSynchronisationService.waitForAllWorkers(CompletedInitializationMessage);

    distributeRunSimulationMessage(dividedPatchesIds);

    log.info("Waiting for end simulation");
    workerSynchronisationService.waitForAllWorkers(FinishSimulationMessage);
    log.info("Simulation finished");

    if (configurationService.getConfiguration().isStatisticModeActive()) {
      workerSynchronisationService.waitForAllWorkers(FinishSimulationStatisticMessage);
      generateReport();
    }
  }

  private void generateReport() {
  }

  private void distributeRunSimulationMessage(Map<String, List<PatchId>> dividedPatchesIds) {
  }

  private List<Patch> createPatches() {
    log.info("Start reading map, and create patches");
    if (configurationService.getConfiguration().isParsedMap()) {
      //ToDo read existing map
    } else {
      // ToDo code to read map from OSM file
    }

    log.info("Patches created successful");
    return null;
  }

  private class PrepareWorkerTask implements Runnable {

    @Override
    public void run() {
      workerStrategyService.executeStrategy();
    }
  }
}
