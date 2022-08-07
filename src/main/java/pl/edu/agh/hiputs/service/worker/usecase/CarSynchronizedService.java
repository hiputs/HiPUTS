package pl.edu.agh.hiputs.service.worker.usecase;

import java.util.List;
import pl.edu.agh.hiputs.communication.model.serializable.SCar;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface CarSynchronizedService {

  void getSerializedCarByPatch(TransferDataHandler transferDataHandler);

  List<SCar> getSerializedCarByPatch(TransferDataHandler transferDataHandler, PatchId patchId);
}
