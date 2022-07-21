package pl.edu.agh.hiputs.loadbalancer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

@Service
@RequiredArgsConstructor
public class SimplyLoadBalancingService implements LoadBalancingStrategy {

  @Override
  public MapFragmentId selectNeighbourToBalancing(TransferDataHandler transferDataHandler) {
    return null;
  }

}
