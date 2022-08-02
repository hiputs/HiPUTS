package pl.edu.agh.hiputs.loadbalancer;

import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface LoadBalancingStrategy {

  MapFragmentId selectNeighbourToBalancing(TransferDataHandler transferDataHandler);

  int getTargetBalanceCarsCount(MapFragmentId recipient);
}
