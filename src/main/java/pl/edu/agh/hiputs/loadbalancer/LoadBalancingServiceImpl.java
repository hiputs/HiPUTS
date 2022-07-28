package pl.edu.agh.hiputs.loadbalancer;

import static pl.edu.agh.hiputs.loadbalancer.utils.CarCounterUtil.countCars;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.loadbalancer.model.BalancingMode;
import pl.edu.agh.hiputs.loadbalancer.model.PatchBalancingInfo;
import pl.edu.agh.hiputs.loadbalancer.utils.CarCounterUtil;
import pl.edu.agh.hiputs.loadbalancer.utils.PatchConnectionSearchUtil;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.PatchTransferService;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadBalancingServiceImpl implements LoadBalancingService {

  private final PatchTransferService patchTransferService;
  private final ConfigurationService configurationService;
  private final SimplyLoadBalancingService simplyLoadBalancingService;
  private final PidLoadBalancingService pidLoadBalancingService;

  @Override
  public void startLoadBalancing(TransferDataHandler transferDataHandler) {

    if (configurationService.getConfiguration().getWorkerCount() == 1
        || configurationService.getConfiguration().getBalancingMode() == BalancingMode.NONE) {
      return;
    }

    if (!shouldBalancingProcess()) {
      return;
    }

    LoadBalancingStrategy strategy = getStrategyByMode();

    MapFragmentId recipient = strategy.selectNeighbourToBalancing(transferDataHandler);
    PatchId patchId = findPatchIdToSend(recipient, transferDataHandler);

    patchTransferService.sendPatch(recipient, transferDataHandler.getPatchById(patchId), transferDataHandler);
  }

  private PatchId findPatchIdToSend(MapFragmentId recipient, TransferDataHandler transferDataHandler) {
    Set<Patch> candidatesToLoadBalancing = transferDataHandler.getBorderPatches().get(recipient);

    List<PatchBalancingInfo> candidatesWithStatistic = candidatesToLoadBalancing.parallelStream().map(patch -> {
      List<PatchId> newNeighbours =
          PatchConnectionSearchUtil.findNeighbouringPatches(patch.getPatchId(), transferDataHandler);
      List<PatchId> removedShadowPatches =
          PatchConnectionSearchUtil.findShadowPatchesNeighbouringOnlyWithPatch(patch.getPatchId(), transferDataHandler);

      return PatchBalancingInfo.builder()
          .patchId(patch.getPatchId())
          .countOfVehicle(countCars(patch))
          .newBorderPatchesAfterTransfer(newNeighbours)
          .countCarsInNewBorderPatches(countCars(newNeighbours, transferDataHandler))
          .shadowPatchesToRemoveAfterTransfer(removedShadowPatches)
          .countCarsInRemovedShadowPatches(countCars(removedShadowPatches, transferDataHandler))
          .build();
    }).toList();

    PatchBalancingInfo candidate =
        candidatesWithStatistic.stream().max(Comparator.comparingInt(this::calculateCost)).get();

    log.info("Select candidate id {} with cost {}", candidate.getPatchId(), calculateCost(candidate));

    return candidate.getPatchId();
  }

  private int calculateCost(PatchBalancingInfo patchBalancingInfo) {
    return
  }

  private MapFragmentId selectRecipient(TransferDataHandler transferDataHandler) {
    return null;
  }

  private boolean shouldBalancingProcess() {
    return true;
  }

  private LoadBalancingStrategy getStrategyByMode() {
    return switch (configurationService.getConfiguration().getBalancingMode()) {
      case SIMPLY -> simplyLoadBalancingService;
      case PID -> pidLoadBalancingService;
      default -> null;
    };
  }
}
