package pl.edu.agh.hiputs.service.worker.usecase;

import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface CarsOnBorderSynchronizationService {

  void sendCarsOnBorderToNeighbours(TransferDataHandler transferDataHandler);

  void synchronizedGetRemoteCars(TransferDataHandler transferDataHandler);

}
