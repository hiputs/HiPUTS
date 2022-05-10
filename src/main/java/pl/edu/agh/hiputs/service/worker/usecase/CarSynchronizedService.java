package pl.edu.agh.hiputs.service.worker.usecase;

import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface CarSynchronizedService {

  void sendCarsToNeighbours(TransferDataHandler transferDataHandler);

  void synchronizedGetIncomingCar(TransferDataHandler transferDataHandler);

}
