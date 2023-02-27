package pl.edu.agh.hiputs.loadbalancer;

import java.util.function.Consumer;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface LoadBalancingService {

  MapFragmentId startLoadBalancing(TransferDataHandler transferDataHandler);

}
