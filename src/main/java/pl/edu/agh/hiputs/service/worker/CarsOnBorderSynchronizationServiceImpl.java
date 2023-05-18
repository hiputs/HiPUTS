package pl.edu.agh.hiputs.service.worker;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.BorderSynchronizationMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.scheduler.task.LaneSerializationTask;
import pl.edu.agh.hiputs.scheduler.task.SynchronizeShadowPatchState;
import pl.edu.agh.hiputs.service.worker.usecase.CarsOnBorderSynchronizationService;
import pl.edu.agh.hiputs.statistics.SimulationPoint;
import pl.edu.agh.hiputs.statistics.worker.IterationStatisticsService;
import pl.edu.agh.hiputs.utils.DebugUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarsOnBorderSynchronizationServiceImpl implements CarsOnBorderSynchronizationService, Subscriber {

  private final WorkerSubscriptionService subscriptionService;
  private final TaskExecutorService taskExecutorService;
  private final MessageSenderService messageSenderService;
  private final List<BorderSynchronizationMessage> incomingMessages = Collections.synchronizedList(new LinkedList<>());
  private final List<BorderSynchronizationMessage> futureIncomingMessages =
      Collections.synchronizedList(new LinkedList<>());
  private final IterationStatisticsService iterationStatisticsService;
  private final AtomicInteger simulationStepNo = new AtomicInteger(0);

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.BorderSynchronizationMessage);
  }

  @Override
  public void sendCarsOnBorderToNeighbours(TransferDataHandler mapFragment) {
    log.info("Step 9-0-1");
    iterationStatisticsService.startStage(SimulationPoint.PATCHES_SERIALIZATION);
    Set<Patch> distinctBorderPatches =
        mapFragment.getBorderPatches().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());

    List<Callable<?>> serializedBorderPatchesTasks = distinctBorderPatches.parallelStream()
        .map(patch -> patch.parallelStreamLanesEditable().map(LaneSerializationTask::new).collect(Collectors.toList()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    // log.info("Step 9-0-3");
    List<Pair<LaneId, byte[]>> serializedBorderLines =
        (List<Pair<LaneId, byte[]>>) taskExecutorService.executeCallableBatch(serializedBorderPatchesTasks);
    Map<PatchId, List<byte[]>> serializedBorderPatches = serializedBorderLines.stream()
        .collect(Collectors.groupingBy(pair -> mapFragment.getPatchIdByLaneId(pair.getLeft()),
            mapping(Pair::getRight, toList())));

    Map<MapFragmentId, BorderSynchronizationMessage> messages = mapFragment.getBorderPatches()
        .entrySet()
        .parallelStream()
        .map(entry -> new ImmutablePair<>(entry.getKey(), createMessageFrom(entry.getValue(), serializedBorderPatches)))
        .collect(Collectors.toConcurrentMap(ImmutablePair::getLeft, ImmutablePair::getRight));

    log.info("Step 9-0-2");
    iterationStatisticsService.endStage(SimulationPoint.PATCHES_SERIALIZATION);
    iterationStatisticsService.startStage(SimulationPoint.PATCHES_SENDING);
    // long time3 = System.currentTimeMillis();
    sendMessages(messages);

    iterationStatisticsService.endStage(SimulationPoint.PATCHES_SENDING);
    // long time4 = System.currentTimeMillis();
    // log.info("Step 9 times: {}, {}; {}; {};", time1-time0,time2-time1, time3-time2, time4-time3);
  }

  @Override
  public synchronized void synchronizedGetRemoteCars(TransferDataHandler mapFragment) {
    int countOfNeighbours = mapFragment.getNeighbors().size();
    int readedMessage = 0;

    while (incomingMessages.size() < countOfNeighbours && readedMessage < countOfNeighbours) {
      try {
        this.wait();
        readedMessage += applyMessages(mapFragment, readedMessage);
      } catch (InterruptedException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

    incomingMessages.clear();
    incomingMessages.addAll(futureIncomingMessages);
    simulationStepNo.incrementAndGet();
    futureIncomingMessages.clear();
  }

  private int applyMessages(TransferDataHandler mapFragment, int readedMessage) {
    List<Runnable> synchronizeShadowPatchesStateTasks = incomingMessages.subList(readedMessage, incomingMessages.size())
        .parallelStream()
        .flatMap(message -> message.getPatchContent().entrySet().stream())
        .map(e -> new SynchronizeShadowPatchState(e.getKey(), e.getValue(), mapFragment))
        .collect(Collectors.toList());

    taskExecutorService.executeBatch(synchronizeShadowPatchesStateTasks);

    return synchronizeShadowPatchesStateTasks.size();
  }

  private String getWeaitingByMessage() {
    try {
      final Set<MapFragmentId> mapFragmentIds = incomingMessages.stream()
          .map(m -> m.getPatchContent().keySet().iterator().next())
          .map(patchId -> DebugUtils.getMapFragment().getMapFragmentIdByPatchId(new PatchId(patchId)))
          .collect(Collectors.toSet());

      return DebugUtils.getMapFragment().getNeighbors()
          .stream()
          .filter(mapFragmentId -> !mapFragmentIds.contains(mapFragmentId))
          .map(mapFragmentId -> mapFragmentId.getId() + "{ " + DebugUtils.getMapFragment().getBorderPatches().get(mapFragmentId).stream().map(p -> p.getPatchId().getValue()).collect(
              Collectors.joining(", ")) + "}")
          .collect(Collectors.joining(", "));
    } catch (Exception e){

    }
    return "---";
  }

  @Override
  public synchronized void notify(Message message) {
    try {
      if (message.getMessageType() != MessagesTypeEnum.BorderSynchronizationMessage) {
        return;
      }

      BorderSynchronizationMessage borderSynchronizationMessage = (BorderSynchronizationMessage) message;
      // log.info("---Receive--- my it {} it {} from {}", simulationStepNo.get(), borderSynchronizationMessage.getSimulationStepNo(), borderSynchronizationMessage.getPatchContent().keySet().iterator().next());
      if (borderSynchronizationMessage.getSimulationStepNo() == simulationStepNo.get()) {
        incomingMessages.add(borderSynchronizationMessage);
        notifyAll();
      } else {
        futureIncomingMessages.add(borderSynchronizationMessage);
      }

    } catch (Exception e) {
      log.error("Unexpected Exception : ", e);
    }
  }

  private void sendMessages(Map<MapFragmentId, BorderSynchronizationMessage> messages) {
    messages.entrySet().parallelStream().forEach(entry -> {
      try {
        messageSenderService.send(entry.getKey(), entry.getValue());
      } catch (IOException e) {
        log.error("Error sending message BorderSynchronizationMessage to: " + entry.getKey(), e);
      }
    });

  }

  private BorderSynchronizationMessage createMessageFrom(Set<Patch> patches,
      Map<PatchId, List<byte[]>> serializedBorderPatches) {
    Map<String, List<byte[]>> patchContent = patches.stream()
        .collect(Collectors.toMap(e -> e.getPatchId().getValue(),
            e -> serializedBorderPatches.get(e.getPatchId())));
    return new BorderSynchronizationMessage(simulationStepNo.get(), patchContent);
  }

}
