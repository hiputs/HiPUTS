package pl.edu.agh.hiputs.loadbalancer;

import pl.edu.agh.hiputs.model.id.MapFragmentId;

public interface LoadBalancingPatchDecider {

  void getPatch(MapFragmentId recipient);
}
