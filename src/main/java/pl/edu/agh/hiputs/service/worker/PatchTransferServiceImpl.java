package pl.edu.agh.hiputs.service.worker;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
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
import pl.edu.agh.hiputs.communication.model.messages.GroupOfPatchTransferNotificationMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferMessage;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferNotificationMessage;
import pl.edu.agh.hiputs.communication.model.messages.SerializedPatchTransfer;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedLane;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.loadbalancer.TicketService;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.scheduler.task.InsertPatchTask;
import pl.edu.agh.hiputs.service.worker.usecase.CarSynchronizationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.service.worker.usecase.PatchTransferService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatchTransferServiceImpl implements Subscriber, PatchTransferService {

  private final MapRepository mapRepository;
  private final WorkerSubscriptionService subscriptionService;
  private final MessageSenderService messageSenderService;
  private final CarSynchronizationService carSynchronizedService;
  private final TaskExecutorService taskExecutorService;

  private final TicketService ticketService;

  private final Queue<PatchTransferMessage> receivedPatch = new LinkedBlockingQueue<>();
  private final Queue<PatchTransferNotificationMessage> patchMigrationNotification = new LinkedBlockingQueue<>();
  private final AtomicInteger retransmittedNotifications = new AtomicInteger(0);

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferNotificationMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.GroupOfPatchTransferNotificationMessage);
  }

  @Override
  public SerializedPatchTransfer prepareSinglePatchItem(MapFragmentId receiver, PatchId patchToSendId,
      TransferDataHandler transferDataHandler) {

    Patch patchToSend = transferDataHandler.getPatchById(patchToSendId);

    List<ImmutablePair<String, String>> neighPatchIdWithMapFragmentId =
        patchToSend.getNeighboringPatches().stream().filter(id -> !patchToSendId.equals(id)).map(id -> {
          try {
            return new ImmutablePair<>(id.getValue(), transferDataHandler.getMapFragmentIdByPatchId(id).getId());
          } catch (NullPointerException e) {
            log.error("Not found mapFragmentId for {}", id.getValue());
            return null;
          }
        }).filter(Objects::nonNull).toList();

    log.debug("neighPatchIdWithMapFragmentId {}",
        neighPatchIdWithMapFragmentId.stream().map(a -> a.getLeft() + ":" + a.getRight()));
    log.debug("Patch to send: {}; Patch owner: {}, neighbours {}", patchToSendId.getValue(),
        transferDataHandler.getMapFragmentIdByPatchId(patchToSendId).getId(), neighPatchIdWithMapFragmentId);

    List<ConnectionDto> neighbourConnectionDtos = neighPatchIdWithMapFragmentId.stream()
        .map(Pair::getRight)
        .distinct()
        .map(MapFragmentId::new)
        .filter(mapFragmentId -> !receiver.equals(mapFragmentId) && !transferDataHandler.getMe().equals(mapFragmentId))
        .map(mapFragmentId -> messageSenderService.getConnectionDtoMap().get(mapFragmentId))
        .toList();
    log.debug("neighbourConnectionDtos {}",
        neighbourConnectionDtos.stream().map(ConnectionDto::getId).collect(Collectors.joining(",")));

    List<SerializedLane> serializedLanes =
        carSynchronizedService.getSerializedCarByPatch(transferDataHandler, patchToSend.getPatchId());

    transferDataHandler.migratePatchToNeighbour(patchToSend, receiver, ticketService);

    return SerializedPatchTransfer.builder()
        .patchId(patchToSend.getPatchId().getValue())
        .neighbourConnectionMessage(neighbourConnectionDtos)
        // .mapFragmentId(transferDataHandler.getMe().getId())
        .patchIdWithMapFragmentId(neighPatchIdWithMapFragmentId)
        .serializedLanes(serializedLanes)
        .build();
  }

  @Override
  public List<MapFragmentId> neighboursToNotify(MapFragmentId receiver, PatchId patchToSendId,
      TransferDataHandler transferDataHandler) {

    return transferDataHandler.getPatchById(patchToSendId)
        .getNeighboringPatches()
        .stream()
        .filter(id -> !patchToSendId.equals(id))
        .map(transferDataHandler::getMapFragmentIdByPatchId)
        .filter(Objects::nonNull)
        .distinct()
        .filter(mapFragmentId -> !receiver.getId().equals(mapFragmentId.getId()) && !transferDataHandler.getMe()
            .getId()
            .equals(mapFragmentId.getId()))
        .collect(Collectors.toList());

  }

  @Override
  public void sendPatchMessage(MapFragmentId sender, MapFragmentId receiver,
      List<SerializedPatchTransfer> serializedPatchTransfers) {
    PatchTransferMessage patchTransferMessage = new PatchTransferMessage(serializedPatchTransfers, sender.getId());
    log.debug("SendPatchMessage from {} to {}", sender.getId(), receiver.getId());
    try {
      messageSenderService.send(receiver, patchTransferMessage);
    } catch (IOException e) {
      log.error("Fail send patch message");
    }
  }

  @Override
  public void handleReceivedPatch(TransferDataHandler transferDataHandler) {
    List<Runnable> tasks =
        receivedPatch.stream().map(message -> message.getSerializedPatchTransferList().stream().map(m -> {
          List<ImmutablePair<PatchId, MapFragmentId>> pairs = m.getPatchIdWithMapFragmentId()
              .stream()
              .map(pair -> new ImmutablePair<>(new PatchId(pair.getLeft()), new MapFragmentId(pair.getRight())))
              .collect(Collectors.toList());

          log.debug("Patch from: {} - {}", message.getMapFragmentId(), new MapFragmentId(message.getMapFragmentId()));
          log.debug("Pairs: {}", pairs.stream().map(a -> a.getLeft().getValue() + ":" + a.getRight().getId()).toList());

          transferDataHandler.migratePatchToMe(new PatchId(m.getPatchId()),
              new MapFragmentId(message.getMapFragmentId()), mapRepository, pairs, ticketService);

          return new InsertPatchTask(new PatchId(m.getPatchId()), m.getSerializedLanes(), transferDataHandler);
        }).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toList());

    taskExecutorService.executeBatch(tasks);
    receivedPatch.clear();
  }


  @Override
  public void handleNotificationPatch(TransferDataHandler transferDataHandler) {
    while (!patchMigrationNotification.isEmpty()) {
      PatchTransferNotificationMessage message = patchMigrationNotification.remove();

      if (message.getReceiverId().equals(transferDataHandler.getMe().getId()) || message.getSenderId()
          .equals(transferDataHandler.getMe().getId())) {
        log.debug("Patch notification not processed");
        continue;
      }

      message.getTransferredPatchesList().forEach(patchId -> {
        log.debug("NOTIFICATION patchId {}, from {}, to {}", patchId, message.getSenderId(), message.getReceiverId());
        transferDataHandler.migratePatchBetweenNeighbour(new PatchId(patchId),
            new MapFragmentId(message.getReceiverId()), new MapFragmentId(message.getSenderId()), ticketService);
      });
    }
  }

  @Override
  public synchronized void retransmitNotification(MapFragmentId selectedCandidate, TransferDataHandler transferDataHandler) {
    if (selectedCandidate == null) {
      return;
    }

    GroupOfPatchTransferNotificationMessage notificationGroup =
        GroupOfPatchTransferNotificationMessage.builder().patchTransferNotificationMessages(new LinkedList<>()).build();

    patchMigrationNotification.stream()
        .filter(notify -> notify.getReceiverId() != null && !notify.getSenderId().equals(selectedCandidate.getId()))
        .forEach(s -> {
          notificationGroup.getPatchTransferNotificationMessages().add(s);
        });

    log.debug("notif group : {}", notificationGroup.getPatchTransferNotificationMessages()
        .stream()
        .map(msg -> msg.getSenderId() + " " + msg.getReceiverId())
        .toList());

    notificationGroup.getPatchTransferNotificationMessages()
        .addAll(receivedPatch.stream()
            .map(message -> PatchTransferNotificationMessage.builder()
                .transferredPatchesList(
                    message.getSerializedPatchTransferList().stream().map(SerializedPatchTransfer::getPatchId).collect(Collectors.toList()))
                .receiverId(transferDataHandler.getMe().getId())
                .senderId(message.getMapFragmentId())
                .connectionDto(messageSenderService.getConnectionDtoMap().get(transferDataHandler.getMe()))
                .build())
            .collect(Collectors.toList()));

    log.debug("notif group : {}", notificationGroup.getPatchTransferNotificationMessages()
        .stream()
        .map(msg -> msg.getSenderId() + " " + msg.getReceiverId())
        .toList());

    try {
      log.debug("Send retransmission to {}", selectedCandidate.getId());
      messageSenderService.send(selectedCandidate, notificationGroup);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public synchronized void synchronizedGetRetransmittedNotification(TransferDataHandler transferDataHandler) {

    //wait for message from workers, which sent you patches
    log.debug("Waiting for retransmission messages from {}; retransmittet notifications status: {} ",
        receivedPatch.size(), retransmittedNotifications.get());
    while (retransmittedNotifications.get() < receivedPatch.size()) {
      try {
        this.wait(10);
        log.debug("Status of retransmittedNotifications: {}", retransmittedNotifications.get());
      } catch (InterruptedException e) {
        log.error("error until wait for loadbalancing synchronization", e);
      }
    }
    retransmittedNotifications.set(0);
  }

  @Override
  public synchronized void notify(Message message) {
    if (message instanceof PatchTransferNotificationMessage) {
      handlePatchTransferNotificationMessage((PatchTransferNotificationMessage) message);
    } else if (message instanceof PatchTransferMessage) {
      handlePatchTransferMessage((PatchTransferMessage) message);
    } else if (message instanceof GroupOfPatchTransferNotificationMessage) {
      ((GroupOfPatchTransferNotificationMessage) message).getPatchTransferNotificationMessages()
          .forEach(this::handlePatchTransferNotificationMessage);
      retransmittedNotifications.incrementAndGet();
      this.notifyAll();
    }
  }

  private void handlePatchTransferMessage(PatchTransferMessage message) {
    receivedPatch.add(message);
  }

  private void handlePatchTransferNotificationMessage(PatchTransferNotificationMessage message) {
    // log.info("The patch id: " + message.getTransferPatchId() + " change owner from " + message.getSenderId() + " to "
    //     + message.getReceiverId());
    if (message.getReceiverId() != null && message.getTransferredPatchesList() != null) {
      patchMigrationNotification.add(message);
    }
  }
}
