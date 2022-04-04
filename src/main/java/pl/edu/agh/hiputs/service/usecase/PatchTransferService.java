package pl.edu.agh.hiputs.service.usecase;

import pl.edu.agh.hiputs.model.id.ActorId;
import pl.edu.agh.hiputs.model.id.PatchId;

public interface PatchTransferService {

    void sendPatch(ActorId receiver, PatchId patchId);

    void getReceivedPatch();
}
