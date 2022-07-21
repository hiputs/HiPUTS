package pl.edu.agh.hiputs.loadbalancer;

import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.hiputs.model.id.MapFragmentId;

public interface LoadStatisticService {

  int getMyLastLoad();

  List<Integer> getMyLastLoads(int size);

  List<ImmutablePair<MapFragmentId, Integer>> getNeighbourLoad();
}
