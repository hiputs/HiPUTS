package pl.edu.agh.hiputs.loadbalancer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

@Service
@RequiredArgsConstructor
public class PidLoadBalancingService implements LoadBalancingStrategy{

  @Override
  public LoadBalancingDecision makeBalancingDecision(TransferDataHandler transferDataHandler) {
    return null;
  }
}
