package pl.edu.agh.hiputs.loadbalancer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.loadbalancer.model.BalancingMode;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.PatchTransferService;

@Service
@RequiredArgsConstructor
public class LoadBalancingServiceImpl implements LoadBalancingService{

  private final PatchTransferService patchTransferService;
  private final ConfigurationService configurationService;
  private final SimplyLoadBalancingService simplyLoadBalancingService;
  private final PidLoadBalancingService pidLoadBalancingService;


  @Override
  public void startLoadBalancing(TransferDataHandler transferDataHandler) {

    if(configurationService.getConfiguration().getWorkerCount() == 1 || configurationService.getConfiguration().getBalancingMode() == BalancingMode.NONE){
      return;
    }

    if(!shouldBalancingProcess()){
      return;
    }

    LoadBalancingStrategy strategy = getStrategyByMode();

    MapFragmentId recipient = strategy.selectNeighbourToBalancing(transferDataHandler);
    PatchId patchId = findPatchIdToSend(recipient, transferDataHandler);

    patchTransferService.sendPatch(recipient, transferDataHandler.getPatchById(patchId), transferDataHandler);
  }

  private PatchId findPatchIdToSend(MapFragmentId recipient, TransferDataHandler transferDataHandler) {
  }

  private MapFragmentId selectRecipient(TransferDataHandler transferDataHandler) {
    return null;
  }

  private boolean shouldBalancingProcess() {
    return true;
  }

  private LoadBalancingStrategy getStrategyByMode(){
    return switch (configurationService.getConfiguration().getBalancingMode()){
      case SIMPLY -> simplyLoadBalancingService;
      case PID -> pidLoadBalancingService;
      default -> null;
    };
  }
}
