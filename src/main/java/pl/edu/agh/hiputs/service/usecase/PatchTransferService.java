package pl.edu.agh.hiputs.service.usecase;

import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.Patch;

public interface PatchTransferService {

    void sendPatch(MapFragmentId receiver, Patch patch);

    void getReceivedPatch();
}
