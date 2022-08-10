package pl.edu.agh.hiputs.loadbalancer;

import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface LoadBalancingStrategy {

  LoadBalancingDecision makeBalancingDecision(TransferDataHandler transferDataHandler);

  @Getter
  @Setter
  class LoadBalancingDecision {
    private boolean loadBalancingRecommended;
    private MapFragmentId selectedNeighbour;
    private long carImbalanceRate;
  }
}
