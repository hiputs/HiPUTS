package pl.edu.agh.hiputs.startingUp;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.CompletedInitializationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.FinishSimulationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.FinishSimulationStatisticMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.WorkerConnectionMessage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.MapReadyToReadMessage;
import pl.edu.agh.hiputs.communication.service.server.MessageSenderServerService;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.service.MapFragmentPartitioner;
import pl.edu.agh.hiputs.partition.service.MapStructureLoader;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.server.WorkerSynchronisationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerStrategyService implements Strategy {

  private final WorkerSynchronisationService workerSynchronisationService;
  private final ConfigurationService configurationService;
  private final WorkerStrategyService workerStrategyService;
  private final MapFragmentPartitioner mapFragmentPartitioner;
  private final ExecutorService workerPrepareExecutor = newSingleThreadExecutor();
  private final MessageSenderServerService messageSenderServerService;
  private final MapStructureLoader mapStructureLoader;

  @Override
  public void executeStrategy() {

    log.info("Running server");
    workerPrepareExecutor.submit(new PrepareWorkerTask());

    Graph<PatchData, PatchConnectionData> patchesGraph = createPatches();
    Collection<Graph<PatchData, PatchConnectionData>> mapFragmentsContents = mapFragmentPartitioner.partition(patchesGraph);

    log.info("Start waiting for all workers be in state WorkerConnection");
    workerSynchronisationService.waitForAllWorkers(WorkerConnectionMessage);

    if (configurationService.getConfiguration().isParsedMap()) {
      messageSenderServerService.broadcast(new MapReadyToReadMessage());
    }

    log.info("Waiting for all workers by in state CompletedInitialization");
    workerSynchronisationService.waitForAllWorkers(CompletedInitializationMessage);

    distributeRunSimulationMessage(mapFragmentsContents);

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

  private void distributeRunSimulationMessage(Collection<Graph<PatchData, PatchConnectionData>> dividedPatchesIds) {

  }

  private Graph<PatchData, PatchConnectionData> createPatches() {
    log.info("Start reading map");
    Graph<PatchData, PatchConnectionData> patchesGraph;
    if (configurationService.getConfiguration().isParsedMap()) { //todo change property name to "createPatches" or "readFromOsmDirectly"
      //read existing map
      log.info("Reading map from import package - patch partition skipped");
      patchesGraph = mapStructureLoader.loadFromCsvImportPackage(Path.of(configurationService.getConfiguration().getMapPath()));
    } else {
      //read map from OSM file
      log.info("Reading map from osm file");
      try {
        patchesGraph = mapStructureLoader.loadFromOsmFile(Path.of(configurationService.getConfiguration().getMapPath()));
      } catch (IOException e) {
        log.error(e.getMessage());
        return null;
      }
    }

    log.info("Reading map finished successfully");
    return patchesGraph;
  }

  private class PrepareWorkerTask implements Runnable {

    @Override
    public void run() {
      workerStrategyService.executeStrategy();
    }
  }
}
