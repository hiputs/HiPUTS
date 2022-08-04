package pl.edu.agh.hiputs.service.worker.usecase;

import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface IncomingCarsSetsSynchronizationService {

  void sendIncomingSetsOfCarsToNeighbours(TransferDataHandler transferDataHandler);

  void synchronizedGetIncomingSetsOfCars(TransferDataHandler transferDataHandler);

}
