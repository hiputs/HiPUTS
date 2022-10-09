package pl.edu.agh.hiputs.service.worker.usecase;

import java.util.List;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedCar;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface CarSynchronizationService {

  void sendIncomingSetsOfCarsToNeighbours(TransferDataHandler transferDataHandler);

  void synchronizedGetIncomingSetsOfCars(TransferDataHandler transferDataHandler);

  List<SerializedCar> getSerializedCarByPatch(TransferDataHandler transferDataHandler, PatchId patchId);

}
