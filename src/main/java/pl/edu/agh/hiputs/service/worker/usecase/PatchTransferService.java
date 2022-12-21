package pl.edu.agh.hiputs.service.worker.usecase;

import java.util.List;
import pl.edu.agh.hiputs.communication.model.messages.SerializedPatchTransfer;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface PatchTransferService {

  SerializedPatchTransfer prepareSinglePatchItemAndNotifyNeighbour(MapFragmentId receiver, PatchId patchId, TransferDataHandler transferDataHandler);

  void sendPatchMessage(MapFragmentId receiver, List<SerializedPatchTransfer> serializedPatchTransfers);

  void handleReceivedPatch(TransferDataHandler transferDataHandler);

  void handleNotificationPatch(TransferDataHandler transferDataHandler);

  void retransmitNotification(MapFragmentId selectedCandidate);
}
