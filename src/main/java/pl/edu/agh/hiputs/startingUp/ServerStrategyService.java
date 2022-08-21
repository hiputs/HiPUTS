package pl.edu.agh.hiputs.startingUp;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.CompletedInitializationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.FinishSimulationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.FinishSimulationStatisticMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.WorkerConnectionMessage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.MapReadyToReadMessage;
import pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage;
import pl.edu.agh.hiputs.communication.model.messages.RunSimulationMessage;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.communication.model.serializable.WorkerDataDto;
import pl.edu.agh.hiputs.communication.service.server.ConnectionInitializationService;
import pl.edu.agh.hiputs.communication.service.server.MessageSenderServerService;
import pl.edu.agh.hiputs.communication.service.server.WorkerRepository;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.persistance.PatchesGraphReader;
import pl.edu.agh.hiputs.partition.persistance.PatchesGraphWriter;
import pl.edu.agh.hiputs.partition.service.MapFragmentPartitioner;
import pl.edu.agh.hiputs.partition.service.MapStructureLoader;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.server.StatisticSummaryService;
import pl.edu.agh.hiputs.service.server.WorkerSynchronisationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepositoryServerHandler;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerStrategyService implements Strategy {

  private final WorkerSynchronisationService workerSynchronisationService;
  private final ConnectionInitializationService connectionInitializationService;
  private final ConfigurationService configurationService;
  private final WorkerStrategyService workerStrategyService;
  private final MapFragmentPartitioner mapFragmentPartitioner;
  private final ExecutorService workerPrepareExecutor = newSingleThreadExecutor();
  private final MessageSenderServerService messageSenderServerService;
  private final MapStructureLoader mapStructureLoader;
  private final StatisticSummaryService statisticSummaryService;

  private final MapRepositoryServerHandler mapRepository;
  private final PatchesGraphReader patchesGraphReader;

  private final PatchesGraphWriter patchesGraphWriter;

  private final WorkerRepository workerRepository;

  @Override
  public void executeStrategy() {

    log.info("Running server");
    connectionInitializationService.init();
    workerPrepareExecutor.submit(new PrepareWorkerTask());

    Path mapPackagePath = configurationService.getConfiguration().isReadFromOsmDirectly()
        ? generateDeploymentPackageName(Path.of(configurationService.getConfiguration().getMapPath()))
        : Path.of(configurationService.getConfiguration().getMapPath());

    Graph<PatchData, PatchConnectionData> patchesGraph = configurationService.getConfiguration().isReadFromOsmDirectly()
        ? createAndSavePatchesPackage(mapPackagePath)
        : patchesGraphReader.readGraphWithPatches(mapPackagePath);

    mapRepository.setPatchesGraph(patchesGraph);

    log.info("Start waiting for all workers be in state WorkerConnection");
    workerSynchronisationService.waitForAllWorkers(WorkerConnectionMessage);


    if (configurationService.getConfiguration().isReadFromOsmDirectly()) {
      messageSenderServerService.broadcast(MapReadyToReadMessage.builder().mapPackagePath(mapPackagePath.toString()).build());
    }

    Collection<Graph<PatchData, PatchConnectionData>> mapFragmentsContents = mapFragmentPartitioner.partition(patchesGraph);

    calculateAndDistributeConfiguration(mapFragmentsContents);

    log.info("Waiting for all workers be in state CompletedInitialization");
    workerSynchronisationService.waitForAllWorkers(CompletedInitializationMessage);

    distributeRunSimulationMessage(mapFragmentsContents);

    log.info("Waiting for end simulation");
    workerSynchronisationService.waitForAllWorkers(FinishSimulationMessage);
    log.info("Simulation finished");

    if (configurationService.getConfiguration().isStatisticModeActive()) {
      workerSynchronisationService.waitForAllWorkers(FinishSimulationStatisticMessage);
      log.info("Start generating summary");
      generateReport();
    }

    shutDown();
  }

  private void calculateAndDistributeConfiguration(Collection<Graph<PatchData, PatchConnectionData>> mapFragmentsContents) {
    Iterator<String> workerIdsIterator = workerRepository.getAllWorkersIds().iterator();
    Iterator<Graph<PatchData, PatchConnectionData>> mapFragmentContentIterator = mapFragmentsContents.iterator();

    Map<String, String> patchId2workerId = new HashMap<>();
    // map which patch belongs where
    while(workerIdsIterator.hasNext() && mapFragmentContentIterator.hasNext()) {
      String workerId = workerIdsIterator.next();
      Graph<PatchData, PatchConnectionData> mapFragmentContent = mapFragmentContentIterator.next();

      patchId2workerId.putAll(
          mapFragmentContent.getNodes().keySet().stream().collect(Collectors.toMap(Function.identity(), e -> workerId))
      );
    }

    workerIdsIterator = workerRepository.getAllWorkersIds().iterator();
    mapFragmentContentIterator = mapFragmentsContents.iterator();

    //prepare serverInitMessage for each worker
    Map<String, ServerInitializationMessage> workerId2ServerInitializationMessage = new HashMap<>();

    while(workerIdsIterator.hasNext() && mapFragmentContentIterator.hasNext()) {
      String workerId = workerIdsIterator.next();
      Graph<PatchData, PatchConnectionData> mapFragmentContent = mapFragmentContentIterator.next();

      //calculate shadow patches
      Set<String> shadowPatchesIds = mapFragmentContent.getNodes().values()
          .stream().flatMap(n -> Stream.concat(n.getIncomingEdges().stream(), n.getOutgoingEdges().stream()))
          .distinct()
          .flatMap(e -> Stream.of(e.getSource(), e.getTarget()))
          .map(Node::getId)
          .filter(id -> !mapFragmentContent.getNodes().containsKey(id))
          .collect(Collectors.toSet());

      //calculate set of shadow patches belong to which neighbour worker
      Map<String, List<String>> workerId2shadowPatchesIds = new HashMap<>();
      shadowPatchesIds.forEach(patchId -> {
        String neighbourWorkerId = patchId2workerId.get(patchId);
          if (workerId2shadowPatchesIds.containsKey(neighbourWorkerId)) {
            workerId2shadowPatchesIds.get(neighbourWorkerId).add(patchId);
          } else {
            workerId2shadowPatchesIds.put(neighbourWorkerId, Stream.of(patchId).collect(Collectors.toList()));
          }
        });

      List<WorkerDataDto> workerDataDtos = workerId2shadowPatchesIds.entrySet()
          .stream()
          .map(e -> new WorkerDataDto(e.getValue(), ConnectionDto.builder()
              .id(e.getKey())
              .address(workerRepository.get(e.getKey()).getAddress())
              .port(workerRepository.get(e.getKey()).getPort())
              .build()))
          .toList();

      ServerInitializationMessage serverInitializationMessage = ServerInitializationMessage.builder()
          .patchIds(mapFragmentContent.getNodes().keySet().stream().toList())
          .workerInfo(workerDataDtos)
          .build();
      workerId2ServerInitializationMessage.put(workerId, serverInitializationMessage);
    }

    workerId2ServerInitializationMessage.entrySet().forEach(
        e -> messageSenderServerService.send(e.getKey(), e.getValue())
    );
  }

  private void generateReport() {
    statisticSummaryService.generateStatisticCSVs();
  }

  private void distributeRunSimulationMessage(Collection<Graph<PatchData, PatchConnectionData>> dividedPatchesIds) {
    messageSenderServerService.broadcast(new RunSimulationMessage());
  }

  private Graph<PatchData, PatchConnectionData> createAndSavePatchesPackage(Path mapPackagePath) {
    log.info("Start reading map");
    Graph<PatchData, PatchConnectionData> patchesGraph;
    if (configurationService.getConfiguration().isReadFromOsmDirectly()) {
      log.info("Reading map from osm file");
      patchesGraph = mapStructureLoader.loadFromOsmFile(Path.of(configurationService.getConfiguration().getMapPath()));

      log.info("Writing map with patches");
      createDeploymentPackageDir(mapPackagePath);
      patchesGraphWriter.saveGraphWithPatches(patchesGraph, mapPackagePath);

    } else {
      //read existing map
      log.info("Reading map from import package - patch partition skipped");
      patchesGraph = mapStructureLoader.loadFromCsvImportPackage(Path.of(configurationService.getConfiguration().getMapPath()));
    }

    log.info("Reading map finished successfully");
    return patchesGraph;
  }

  private Path generateDeploymentPackageName(Path osmFilePath) {
    String fileName = osmFilePath.getFileName().toString().split("\\.")[0];
    return Paths.get(osmFilePath.getParent().toAbsolutePath().toString(), fileName + "_" + UUID.randomUUID());
  }

  private void createDeploymentPackageDir(Path deploymentPackagePath) {
    if (!deploymentPackagePath.toFile().mkdir()) {
      throw new RuntimeException(String.format("Directory with path %s cannot be created", deploymentPackagePath));
    }
  }

  @Autowired
  private ApplicationContext context;
  private void shutDown() {
    int exitCode = SpringApplication.exit(context, (ExitCodeGenerator) () -> 0);
    System.exit(exitCode);
  }

  private class PrepareWorkerTask implements Runnable {

    @Override
    public void run() {
      try {
        Thread.sleep(1000);
        workerStrategyService.executeStrategy();
      } catch (InterruptedException e) {
        log.error("Worker not started", e);
      } catch (Exception e) {
        log.error("Unexpected exception occurred", e);
      }
    }
  }
}
