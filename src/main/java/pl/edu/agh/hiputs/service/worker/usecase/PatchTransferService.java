package pl.edu.agh.hiputs.service.worker.usecase;

import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;

public interface PatchTransferService {

  void sendPatch(MapFragmentId receiver, Patch patch, TransferDataHandler transferDataHandler);

  void handleReceivedPatch(TransferDataHandler transferDataHandler);

  void handleNotificationPatch(TransferDataHandler transferDataHandler);
}
