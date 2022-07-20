package pl.edu.agh.hiputs.loadbalancer;

import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface LoadBalancingService {

  void startLoadBalancing(TransferDataHandler transferDataHandler);

}
