package pl.edu.agh.service.usecase;

import pl.edu.agh.model.id.ActorId;
import pl.edu.agh.model.id.PatchId;

public interface PatchTransferService {

    void sendPatch(ActorId receiver, PatchId patchId);

    void getReceivedPatch();
}
