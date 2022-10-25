package pl.edu.agh.hiputs.service.worker;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.BorderSynchronizationMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedLane;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.scheduler.task.SynchronizeShadowPatchState;
import pl.edu.agh.hiputs.service.worker.usecase.CarsOnBorderSynchronizationService;
import pl.edu.agh.hiputs.utils.DebugUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarsOnBorderSynchronizationServiceImpl implements CarsOnBorderSynchronizationService, Subscriber {

  private final SubscriptionService subscriptionService;

  private final TaskExecutorService taskExecutorService;

  private final MessageSenderService messageSenderService;
  private final List<BorderSynchronizationMessage> incomingMessages = Collections.synchronizedList(new LinkedList<>());
  private final List<BorderSynchronizationMessage> futureIncomingMessages = Collections.synchronizedList(new LinkedList<>());

  private int simulationStepNo = 0;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.BorderSynchronizationMessage);
  }

  @Override
  public void sendCarsOnBorderToNeighbours(TransferDataHandler mapFragment) {
    Map<MapFragmentId, BorderSynchronizationMessage> messages = mapFragment.getBorderPatches()
        .entrySet()
        .parallelStream()
        .map(entry -> new ImmutablePair<>(entry.getKey(), createMessageFrom(entry.getValue())))
        .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));

    sendMessages(messages);
  }

  @Override
  public synchronized void synchronizedGetRemoteCars(TransferDataHandler mapFragment) {
    int countOfNeighbours = mapFragment.getNeighbors().size();
    log.info("STEP 9 -> {}", mapFragment.getNeighbors().stream().map(MapFragmentId::getId).collect(Collectors.joining(", ")));
    while (incomingMessages.size() < countOfNeighbours) {
      try {
        this.wait(1000);
        log.warn("Waiting for STEP 9: {}", getWeaitingByMessage());
      } catch (InterruptedException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

    List<Runnable> synchronizeShadowPatchesStateTasks = incomingMessages.parallelStream()
        .flatMap(message -> message.getPatchContent().entrySet().parallelStream())
        .map(e -> new SynchronizeShadowPatchState(e.getKey(), e.getValue(), mapFragment))
        .collect(Collectors.toList());

    taskExecutorService.executeBatch(synchronizeShadowPatchesStateTasks);

    incomingMessages.clear();
    incomingMessages.addAll(futureIncomingMessages);
    simulationStepNo++;
    futureIncomingMessages.clear();
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
      if (borderSynchronizationMessage.getSimulationStepNo() == simulationStepNo) {
        incomingMessages.add(borderSynchronizationMessage);
      } else {
        futureIncomingMessages.add(borderSynchronizationMessage);
      }

      notifyAll();
    } catch (Exception e) {
      log.error("Unexpected Exception : ", e);
    }
  }

  private void sendMessages(Map<MapFragmentId, BorderSynchronizationMessage> messages) {
    messages.forEach((mapFragmentId, message) -> {
      try {
        messageSenderService.send(mapFragmentId, message);
      } catch (IOException e) {
        log.error("Error sending message BorderSynchronizationMessage to: " + mapFragmentId, e);
      }
    });
  }

  private BorderSynchronizationMessage createMessageFrom(Set<Patch> patches) {
    Map<String, Set<SerializedLane>> patchContent = patches.stream()
        .collect(Collectors.toMap(e -> e.getPatchId().getValue(),
            e -> e.streamLanesEditable().map(SerializedLane::new).collect(Collectors.toSet())));
    return new BorderSynchronizationMessage(simulationStepNo, patchContent);
  }

}
