package pl.edu.agh.hiputs.startingUp;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.CompletedInitializationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.FinishSimulationMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.FinishSimulationStatisticMessage;
import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.WorkerConnectionMessage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.MapReadyToReadMessage;
import pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage;
import pl.edu.agh.hiputs.communication.model.messages.RunSimulationMessage;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.communication.model.serializable.WorkerDataDto;
import pl.edu.agh.hiputs.communication.service.server.MessageSenderServerService;
import pl.edu.agh.hiputs.communication.service.server.WorkerConnection;
import pl.edu.agh.hiputs.communication.service.server.WorkerRepository;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.persistance.PatchesGraphReader;
import pl.edu.agh.hiputs.partition.service.MapFragmentPartitioner;
import pl.edu.agh.hiputs.partition.service.MapStructureLoader;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.server.WorkerSynchronisationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepositoryServerHandler;

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

  private final MapRepositoryServerHandler mapRepository;
  private final PatchesGraphReader patchesGraphReader;

  private final WorkerRepository workerRepository;

  @Override
  public void executeStrategy() {

    log.info("Running server");
    workerPrepareExecutor.submit(new PrepareWorkerTask());

    Graph<PatchData, PatchConnectionData> patchesGraph = configurationService.getConfiguration().isReadFromOsmDirectly()
        ? createPatches()
        : patchesGraphReader.readGraphWithPatches(Path.of(configurationService.getConfiguration().getMapPath()).getParent());

    mapRepository.setPatchesGraph(patchesGraph);
    Collection<Graph<PatchData, PatchConnectionData>> mapFragmentsContents = mapFragmentPartitioner.partition(patchesGraph);

    log.info("Start waiting for all workers be in state WorkerConnection");
    workerSynchronisationService.waitForAllWorkers(WorkerConnectionMessage);

    if (configurationService.getConfiguration().isReadFromOsmDirectly()) {
      messageSenderServerService.broadcast(new MapReadyToReadMessage());
    }

    calculateAndDistributeConfiguration(mapFragmentsContents);

    log.info("Waiting for all workers be in state CompletedInitialization");
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

  private void calculateAndDistributeConfiguration(Collection<Graph<PatchData, PatchConnectionData>> mapFragmentsContents) {
    Iterator<String> workerIdsIterator = workerRepository.getAllWorkersIds().iterator();
    Iterator<Graph<PatchData, PatchConnectionData>> mapFragmentContentIterator = mapFragmentsContents.iterator();

    Map<String, String> patchId2workerId = new HashMap<>();

    while(workerIdsIterator.hasNext() && mapFragmentContentIterator.hasNext()) {
      String workerId = workerIdsIterator.next();
      Graph<PatchData, PatchConnectionData> mapFragmentContent = mapFragmentContentIterator.next();

      patchId2workerId.putAll(
          mapFragmentContent.getNodes().keySet().stream().collect(Collectors.toMap(Function.identity(), e -> workerId))
      );
    }

    workerIdsIterator = workerRepository.getAllWorkersIds().iterator();
    mapFragmentContentIterator = mapFragmentsContents.iterator();

    Map<String, ServerInitializationMessage> workerId2ServerInitializationMessage = new HashMap<>();

    while(workerIdsIterator.hasNext() && mapFragmentContentIterator.hasNext()) {
      String workerId = workerIdsIterator.next();
      Graph<PatchData, PatchConnectionData> mapFragmentContent = mapFragmentContentIterator.next();

      Set<String> shadowPatchesIds = mapFragmentContent.getEdges().values()
          .stream().flatMap(e -> Stream.of(e.getSource(), e.getTarget()))
          .distinct()
          .map(Node::getId)
          .filter(id -> !mapFragmentContent.getNodes().containsKey(id))
          .collect(Collectors.toSet());

      Map<String, List<String>> workerConnection2shadowPatchesIds = new HashMap<>();
      shadowPatchesIds.forEach(patchId -> {
        String neighbourWorkerId = patchId2workerId.get(patchId);
          if (workerConnection2shadowPatchesIds.containsKey(neighbourWorkerId)) {
            workerConnection2shadowPatchesIds.get(neighbourWorkerId).add(patchId);
          } else {
            workerConnection2shadowPatchesIds.put(neighbourWorkerId, Stream.of(patchId).collect(Collectors.toList()));
          }
        });

      List<WorkerDataDto> workerDataDtos = workerConnection2shadowPatchesIds.entrySet()
          .stream()
          .map(e -> Map.entry(workerRepository.get(e.getKey()), e.getValue()))
          .map(e -> new WorkerDataDto(e.getValue(), ConnectionDto.builder()
              .id(e.getKey().getWorkerId())
              .address(e.getKey().getAddress())
              .port(e.getKey().getPort())
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
  }

  private void distributeRunSimulationMessage(Collection<Graph<PatchData, PatchConnectionData>> dividedPatchesIds) {
    messageSenderServerService.broadcast(new RunSimulationMessage());
  }

  private Graph<PatchData, PatchConnectionData> createPatches() {
    log.info("Start reading map");
    Graph<PatchData, PatchConnectionData> patchesGraph;
    if (configurationService.getConfiguration().isReadFromOsmDirectly()) {
      //read map from OSM file
      log.info("Reading map from osm file");
      try {
        patchesGraph = mapStructureLoader.loadFromOsmFile(Path.of(configurationService.getConfiguration().getMapPath()));
      } catch (IOException e) {
        log.error(e.getMessage());
        return null;
      }
    } else {
      //read existing map
      log.info("Reading map from import package - patch partition skipped");
      patchesGraph = mapStructureLoader.loadFromCsvImportPackage(Path.of(configurationService.getConfiguration().getMapPath()));
    }

    log.info("Reading map finished successfully");
    return patchesGraph;
  }

  private class PrepareWorkerTask implements Runnable {

    @Override
    public void run() {
      try {
        Thread.sleep(1000);
        workerStrategyService.executeStrategy();
      } catch (InterruptedException e) {
        log.error("Worker not started", e);
      }
    }
  }
}
