package pl.edu.agh.hiputs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferMessage;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferNotificationMessage;
import pl.edu.agh.hiputs.communication.model.serializable.SLane;
import pl.edu.agh.hiputs.communication.service.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.SubscriptionService;
import pl.edu.agh.hiputs.model.actor.MapFragment;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.Patch;
import pl.edu.agh.hiputs.service.usecase.PatchTransferService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

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
        //TODO add me real id
        meId = new MapFragmentId("ALA BEZ KOTA");
    }

    @Override
    public void sendPatch(MapFragmentId receiver, Patch patch) {
        // TODO: restore parallel stream?
        List<SLane> serializedLanes = patch
                .streamLanesEditable()
                .map(SLane::new)
                .collect(Collectors.toList());

        PatchTransferMessage patchTransferMessage = PatchTransferMessage
                .builder()
                .patchId(patch.getId().getValue())
                .sLanes(serializedLanes)
                .build();

        PatchTransferNotificationMessage patchTransferNotificationMessage = PatchTransferNotificationMessage
                .builder()
                .transferPatchId(patch.getId().getValue())
                .receiverId(receiver.getId())
                .senderId(meId.getId())
                .build();

        try {
            messageSenderService.send(receiver, patchTransferMessage);
            messageSenderService.broadcast(patchTransferNotificationMessage);
            // TODO fix for new structure (see MapFragment)
//            mapFragment.migrateMyPatchToNeighbour(patchId, receiver);
        } catch (IOException e) {
            log.error("Could not send patch to " + receiver.getId());
        }
    }

    @Override
    public void getReceivedPatch() {
        while (!receivedPatch.isEmpty()) {
            PatchTransferMessage message = receivedPatch.remove();
            // TODO fix for new structure (see MapFragment)
//            mapFragment.migratePatchToMe(new PatchId(message.getPatchId()));

            // TODO this does nothing?
            message.getSLanes()
                    .parallelStream()
                    .map(sLane -> mapFragment.getLaneEditable(new LaneId(sLane.getLaneId())));
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
        log.info("The patch id: " + message.getTransferPatchId() +
                " change owner from " + message.getSenderId() +
                " to " + message.getReceiverId());
        
        // TODO fix for new structure (see MapFragment)
//        mapFragment.getPatch2Actor().put(
//                new PatchId(message.getTransferPatchId()),
//                new MapFragmentId(message.getReceiverId()));
    }
}
