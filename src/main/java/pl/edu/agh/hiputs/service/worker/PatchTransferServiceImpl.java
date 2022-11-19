package pl.edu.agh.hiputs.service.worker;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferMessage;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferNotificationMessage;
import pl.edu.agh.hiputs.communication.model.messages.SerializedPatchTransfer;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedCar;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.loadbalancer.TicketService;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.scheduler.task.InjectIncomingCarsTask;
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

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferNotificationMessage);
  }

  @Override
  public SerializedPatchTransfer prepareSinglePatchItemAndNotifyNeighbour(MapFragmentId receiver, PatchId patchId,
      TransferDataHandler transferDataHandler) {

    Patch patch = transferDataHandler.getPatchById(patchId);

    List<SerializedCar> cars = carSynchronizedService.getSerializedCarByPatch(transferDataHandler, patch.getPatchId());

    List<ImmutablePair<String, String>> patchIdWithMapFragmentId = patch.getNeighboringPatches()
        .stream()
        .filter(id -> !patchId.equals(id))
        .map(id -> {
          try{
            return new ImmutablePair<>(id.getValue(),
                transferDataHandler.getMapFragmentIdByPatchId(id).getId());
          } catch (NullPointerException e){
            log.error("Not found mapFragmentId for {}", id.getValue());
            return  null;
          }
        } )
        .filter(Objects::nonNull)
        .toList();

    List<ConnectionDto> neighbourConnectionDtos = patchIdWithMapFragmentId.stream()
        .map(Pair::getRight)
        .distinct()
        .map(MapFragmentId::new)
        .filter(mapFragmentId -> !receiver.equals(mapFragmentId) && !transferDataHandler.getMe().equals(mapFragmentId))
        .map(mapFragmentId -> messageSenderService.getConnectionDtoMap().get(mapFragmentId))
        .toList();

    transferDataHandler.migratePatchToNeighbour(patch, receiver, ticketService);

    PatchTransferNotificationMessage patchTransferNotificationMessage = PatchTransferNotificationMessage.builder()
        .transferPatchId(patch.getPatchId().getValue())
        .receiverId(receiver.getId())
        .senderId(transferDataHandler.getMe().getId())
        .connectionDto(messageSenderService.getConnectionDtoMap().get(receiver))
        .build();

    neighbourConnectionDtos.forEach(connectionDto -> {
      try {
        messageSenderService.send(new MapFragmentId(connectionDto.getId()), patchTransferNotificationMessage);
      } catch (IOException e) {
        log.error("Worker have not notification about match migration" + connectionDto.getId(), e);
      }
    });

    return SerializedPatchTransfer.builder()
        .patchId(patch.getPatchId().getValue())
        .neighbourConnectionMessage(neighbourConnectionDtos)
        .mapFragmentId(transferDataHandler.getMe().getId())
        .patchIdWithMapFragmentId(patchIdWithMapFragmentId)
        .cars(cars)
        .build();
  }

  @Override
  public void sendPatchMessage(MapFragmentId receiver, List<SerializedPatchTransfer> serializedPatchTransfers) {
    PatchTransferMessage patchTransferMessage = new PatchTransferMessage(serializedPatchTransfers);
    try {
      messageSenderService.send(receiver, patchTransferMessage);
    } catch (IOException e) {
      log.error("Fail send patch message");
    }
  }

  @Override
  public void handleReceivedPatch(TransferDataHandler transferDataHandler) {
    receivedPatch.forEach(message -> {
      message.getSerializedPatchTransferList().forEach(m -> insertPatch(m, transferDataHandler));
    });

    receivedPatch.clear();
  }

  private void insertPatch(SerializedPatchTransfer message, TransferDataHandler transferDataHandler) {
    List<ImmutablePair<PatchId, MapFragmentId>> pairs = message.getPatchIdWithMapFragmentId()
        .stream()
        .map(pair -> new ImmutablePair<>(new PatchId(pair.getLeft()), new MapFragmentId(pair.getRight())))
        .toList();

    transferDataHandler.migratePatchToMe(new PatchId(message.getPatchId()),
        new MapFragmentId(message.getMapFragmentId()), mapRepository, pairs, ticketService);

    InjectIncomingCarsTask task = new InjectIncomingCarsTask(message.getCars(), transferDataHandler);

    taskExecutorService.executeBatch(List.of(task));
  }

  @Override
  public void handleNotificationPatch(TransferDataHandler transferDataHandler) {
    while (!patchMigrationNotification.isEmpty()) {
      PatchTransferNotificationMessage message = patchMigrationNotification.remove();

      if (message.getReceiverId().equals(transferDataHandler.getMe().getId()) ||
          message.getSenderId().equals(transferDataHandler.getMe().getId())) {
        continue;
      }

      transferDataHandler.migratePatchBetweenNeighbour(new PatchId(message.getTransferPatchId()),
          new MapFragmentId(message.getReceiverId()), new MapFragmentId(message.getSenderId()), ticketService);
    }
  }

  @Override
  public void retransmitNotification(MapFragmentId selectedCandidate) {
    if(selectedCandidate == null) {
      return;
    }

    patchMigrationNotification.forEach(s -> {
      try {
        messageSenderService.send(selectedCandidate, s);
      } catch (IOException e) {
        log.error("Retransmit error", e);
      }
    });
  }

  @Override
  public void notify(Message message) {
    if (message instanceof PatchTransferNotificationMessage) {
      handlePatchTransferNotificationMessage((PatchTransferNotificationMessage) message);
    }

    if (message instanceof PatchTransferMessage) {
      handlePatchTransferMessage((PatchTransferMessage) message);
    }
  }

  private void handlePatchTransferMessage(PatchTransferMessage message) {
    receivedPatch.add(message);
  }

  private void handlePatchTransferNotificationMessage(PatchTransferNotificationMessage message) {
    // log.info("The patch id: " + message.getTransferPatchId() + " change owner from " + message.getSenderId() + " to "
    //     + message.getReceiverId());
    patchMigrationNotification.add(message);
  }
}
