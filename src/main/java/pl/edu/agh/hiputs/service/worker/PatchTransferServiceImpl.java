package pl.edu.agh.hiputs.service.worker;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
import pl.edu.agh.hiputs.communication.model.serializable.SerializedCar;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
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
  private final SubscriptionService subscriptionService;
  private final MessageSenderService messageSenderService;
  private final CarSynchronizationService carSynchronizedService;
  private final TaskExecutorService taskExecutorService;

  private final Queue<PatchTransferMessage> receivedPatch = new LinkedList<>();
  private final Queue<PatchTransferNotificationMessage> patchMigrationNotification = new LinkedList<>();

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferNotificationMessage);
  }

  @Override
  public void sendPatch(MapFragmentId receiver, PatchId patchId, TransferDataHandler transferDataHandler) {

    Patch patch = transferDataHandler.getPatch(patchId);

    List<SerializedCar> cars = carSynchronizedService.getSerializedCarByPatch(transferDataHandler, patch.getPatchId());

    List<ImmutablePair<String, String>> patchIdWithMapFragmentId = patch.getNeighboringPatches()
        .stream()
        .map(id -> new ImmutablePair<>(id.getValue(),
            transferDataHandler.getMapFragmentIdByPatchId(patch.getPatchId()).getId()))
        .toList();

    List<ConnectionDto> neighbourConnectionDtos = patchIdWithMapFragmentId.stream()
        .map(Pair::getRight)
        .distinct()
        .map(MapFragmentId::new)
        .filter(mapFragmentId -> !receiver.equals(mapFragmentId))
        .map(mapFragmentId -> messageSenderService.getConnectionDtoMap().get(mapFragmentId))
        .toList();

    PatchTransferMessage patchTransferMessage = PatchTransferMessage.builder()
        .patchId(patch.getPatchId().getValue())
        .neighbourConnectionMessage(neighbourConnectionDtos)
        .mapFragmentId(transferDataHandler.getMe().getId())
        .patchIdWithMapFragmentId(patchIdWithMapFragmentId)
        .cars(cars)
        .build();

    try {
      transferDataHandler.migratePatchToNeighbour(patch, receiver);
      messageSenderService.send(receiver, patchTransferMessage);

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
    } catch (IOException e) {
      log.error("Could not send patch to " + receiver.getId());
    }
  }

  @Override
  public void handleReceivedPatch(TransferDataHandler transferDataHandler) {
    while (!receivedPatch.isEmpty()) {
      PatchTransferMessage message = receivedPatch.remove();
      List<ImmutablePair<PatchId, MapFragmentId>> pairs = message.getPatchIdWithMapFragmentId()
          .stream()
          .map(pair -> new ImmutablePair<>(new PatchId(pair.getValue()), new MapFragmentId(pair.getKey())))
          .toList();

      transferDataHandler.migratePatchToMe(new PatchId(message.getPatchId()),
          new MapFragmentId(message.getMapFragmentId()), mapRepository, pairs);

      InjectIncomingCarsTask task = new InjectIncomingCarsTask(message.getCars(), transferDataHandler);

      taskExecutorService.executeBatch(List.of(task));
    }
  }

  @Override
  public void handleNotificationPatch(TransferDataHandler transferDataHandler) {
    while (!patchMigrationNotification.isEmpty()) {
      PatchTransferNotificationMessage message = patchMigrationNotification.remove();

      transferDataHandler.migratePatchBetweenNeighbour(
          new PatchId(message.getTransferPatchId()),
          new MapFragmentId(message.getReceiverId()),
          new MapFragmentId(message.getSenderId()));
    }
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
    log.info("The patch id: " + message.getTransferPatchId() + " change owner from " + message.getSenderId() + " to "
        + message.getReceiverId());
    patchMigrationNotification.add(message);
  }
}
