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

  @Override
  public void executeStrategy() {
    workerPrepareExecutor.submit(new PrepareWorkerTask());
    workerSynchronisationService.waitForAllWorkers(WorkerConnectionMessage);
    List<Patch> patches = createPatches();
    Map<String, List<PatchId>> dividedPatchesIds = divideService.divide(patches);
    workerSynchronisationService.waitForAllWorkers(CompletedInitializationMessage);
    distributeRunSimulationMessage();
    workerSynchronisationService.waitForAllWorkers(FinishSimulationMessage);

    if (configurationService.getConfiguration().isStatisticModeActive()) {
      workerSynchronisationService.waitForAllWorkers(FinishSimulationStatisticMessage);
      generateReport();
    }
  }

  private void generateReport() {
  }

  private void distributeRunSimulationMessage() {
  }

  private void distributeInitializationData(List<Patch> patches) {
  }

  private List<Patch> createPatches() {
    // ToDo code to read map and create patches
    return null;
  }

  private class PrepareWorkerTask implements Runnable {

    @Override
    public void run() {
      workerStrategyService.executeStrategy();
    }
  }
}
