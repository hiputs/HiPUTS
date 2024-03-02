package pl.edu.agh.hiputs.service.worker.usecase;

import java.util.List;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedLane;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface CarSynchronizationService {

  int sendIncomingSetsOfCarsToNeighbours(TransferDataHandler transferDataHandler);

  void synchronizedGetIncomingSetsOfCars(TransferDataHandler transferDataHandler);

  List<SerializedLane> getSerializedCarByPatch(TransferDataHandler transferDataHandler, PatchId patchId);

}
