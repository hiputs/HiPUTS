package pl.edu.agh.hiputs.service.worker;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferMessage;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferNotificationMessage;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.worker.usecase.PatchTransferService;

@RequiredArgsConstructor
@Slf4j
public class PatchTransferServiceImpl implements Subscriber, PatchTransferService {

  private final MapFragment mapFragment;
  private final SubscriptionService subscriptionService;
  private final MessageSenderService messageSenderService;
  private MapFragmentId meId;

  private final Queue<PatchTransferMessage> receivedPatch = new LinkedList<>();

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PathTransferNotificationMessage);
    meId = MapFragmentId.from(mapFragment.getMapFragmentId());
  }

  @Override
  public void sendPatch(MapFragmentId receiver, Patch patch) {
    List<ConnectionDto> neighbourConnectionDtos = getNeighbourConnectionByPatch(patch, receiver);

    PatchTransferMessage patchTransferMessage = PatchTransferMessage.builder()
        .patchId(patch.getPatchId().getValue())
        .neighbourConnectionMessage(neighbourConnectionDtos)
        .build();


    try {
      mapFragment.migratePatchToNeighbour(patch);
      messageSenderService.send(receiver, patchTransferMessage);

      PatchTransferNotificationMessage patchTransferNotificationMessage = PatchTransferNotificationMessage.builder()
          .transferPatchId(patch.getPatchId().getValue())
          .receiverId(receiver.getId())
          .senderId(meId.getId())
          .build();

      neighbourConnectionDtos.forEach(connectionDto -> {
        try {
          messageSenderService.send(new MapFragmentId(connectionDto.getId()), patchTransferNotificationMessage);
        } catch (IOException e) {
          log.error("Worker have not notification about match migration" + connectionDto.getId(), e);
        }
      });


      // messageSenderService.broadcast(patchTransferNotificationMessage);
      // TODO fix for new structure (see MapFragment)
      //            mapFragment.migrateMyPatchToNeighbour(patchId, receiver);
    } catch (IOException e) {
      log.error("Could not send patch to " + receiver.getId());
    }
  }

  private List<ConnectionDto> getNeighbourConnectionByPatch(Patch patch, MapFragmentId receiver) {
    return mapFragment.getNeighboursMapFragmentIds(patch.getPatchId())
        .stream()
        .filter(mapFragmentId -> !receiver.equals(mapFragmentId))
        .map(mapFragmentId -> messageSenderService.getConnectionDtoMap().get(mapFragmentId))
        .toList();
  }

  @Override
  public void getReceivedPatch() {
    while (!receivedPatch.isEmpty()) {
      PatchTransferMessage message = receivedPatch.remove();
      // TODO fix for new structure (see MapFragment)
      //            mapFragment.migratePatchToMe(new PatchId(message.getPatchId()));

      // TODO this does nothing?
      message.getSLanes().parallelStream().map(sLane -> mapFragment.getLaneEditable(new LaneId(sLane.getLaneId())));
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

    // TODO fix for new structure (see MapFragment)
    //        mapFragment.getPatch2Actor().put(
    //                new PatchId(message.getTransferPatchId()),
    //                new MapFragmentId(message.getReceiverId()));
  }
}
