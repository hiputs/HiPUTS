package pl.edu.agh.hiputs.loadbalancer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

@Service
@RequiredArgsConstructor
public class LoadBalancingServiceImpl implements LoadBalancingService{

  @Override
  public void startLoadBalancing(TransferDataHandler transferDataHandler) {

    if(!shouldBalancingProcess()){
      return;
    }

    MapFragmentId recipient = getRecipient(transferDataHandler);



  }

  private MapFragmentId getRecipient(TransferDataHandler transferDataHandler) {
    return null;
  }

  private boolean shouldBalancingProcess() {
    return true;
  }
}
