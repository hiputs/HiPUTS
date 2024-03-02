package pl.edu.agh.hiputs.service.worker.usecase;

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface CarsOnBorderSynchronizationService {

  List<Pair<String, Integer>> sendCarsOnBorderToNeighbours(TransferDataHandler transferDataHandler);

  void synchronizedGetRemoteCars(TransferDataHandler transferDataHandler);

}
