package pl.edu.agh.hiputs.service.worker;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.BorderSynchronizationMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.serializable.SLane;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.scheduler.task.SynchronizeShadowPatchState;
import pl.edu.agh.hiputs.service.worker.usecase.CarsOnBorderSynchronizationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarsOnBorderSynchronizationServiceImpl implements CarsOnBorderSynchronizationService, Subscriber {

  private final SubscriptionService subscriptionService;

  private final TaskExecutorService taskExecutorService;

  private final MessageSenderService messageSenderService;
  private final List<BorderSynchronizationMessage> incomingMessages = new LinkedList<>();
  private final List<BorderSynchronizationMessage> futureIncomingMessages = new LinkedList<>();

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.BorderSynchronizationMessage);
  }

  @Override
  public void sendCarsOnBorderToNeighbours(TransferDataHandler mapFragment) {
    Map<MapFragmentId, BorderSynchronizationMessage> messages = mapFragment.getBorderPatches()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, e -> createMessageFrom(e.getValue())));

    sendMessages(messages);
  }

  @Override
  public synchronized void synchronizedGetRemoteCars(TransferDataHandler mapFragment) {
    int countOfNeighbours = mapFragment.getNeighbors().size();
    while (incomingMessages.size() < countOfNeighbours) {
      try {
        this.wait();
      } catch (InterruptedException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

    List<Runnable> synchronizeShadowPatchesStateTasks = incomingMessages.stream()
        .flatMap(message -> message.getPatchContent().entrySet().stream())
        .map(e -> new SynchronizeShadowPatchState(e.getKey(), e.getValue(), mapFragment))
        .collect(Collectors.toList());

    taskExecutorService.executeBatch(synchronizeShadowPatchesStateTasks);

    incomingMessages.clear();
    incomingMessages.addAll(futureIncomingMessages);
    futureIncomingMessages.clear();
  }

  @Override
  public synchronized void notify(Message message) {
    try {
      if (message.getMessageType() != MessagesTypeEnum.BorderSynchronizationMessage) {
        return;
      }

      BorderSynchronizationMessage borderSynchronizationMessage = (BorderSynchronizationMessage) message;
      if (incomingMessages.contains(borderSynchronizationMessage)) {
        futureIncomingMessages.add(borderSynchronizationMessage);
      } else {
        incomingMessages.add(borderSynchronizationMessage);
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
    Map<String, Set<SLane>> patchContent = patches.stream()
        .collect(Collectors.toMap(e -> e.getPatchId().getValue(),
            e -> e.streamLanesEditable().map(SLane::new).collect(Collectors.toSet())));
    return new BorderSynchronizationMessage(patchContent);
  }

}
