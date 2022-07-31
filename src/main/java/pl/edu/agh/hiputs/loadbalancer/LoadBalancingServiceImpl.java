package pl.edu.agh.hiputs.loadbalancer;

import static java.util.stream.Collectors.groupingBy;
import static pl.edu.agh.hiputs.loadbalancer.utils.CarCounterUtil.countCars;
import static pl.edu.agh.hiputs.loadbalancer.utils.PatchConnectionSearchUtil.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.loadbalancer.model.BalancingMode;
import pl.edu.agh.hiputs.loadbalancer.model.PatchBalancingInfo;
import pl.edu.agh.hiputs.loadbalancer.utils.CostCalculatorUtil;
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

  private PatchId findPatchIdToSend(MapFragmentId recipient, TransferDataHandler transferDataHandler, int carBalanceTarget) {
    Set<Patch> candidatesToLoadBalancing = transferDataHandler.getBorderPatches().get(recipient);
    List<PatchBalancingInfo> candidatesWithStatistic =
        bindWitchStatistic(candidatesToLoadBalancing, transferDataHandler);

    List<ImmutablePair<PatchBalancingInfo, Double>> orderCandidates = candidatesWithStatistic
        .stream()
        .map(i -> new ImmutablePair<PatchBalancingInfo, Double>(i, CostCalculatorUtil.calculateCost(i, carBalanceTarget)))
        .sorted(Comparator.comparingDouble(ImmutablePair::getRight))
        .toList();


    ImmutablePair<PatchBalancingInfo, Double> selectedCandidate = findFirstCandidateNotLossGraphCoherence(orderCandidates);


    log.info("Select candidate id {} with cost {}", selectedCandidate.getLeft().getPatchId(), selectedCandidate.getRight());

    return selectedCandidate.getLeft().getPatchId();
  }

  private ImmutablePair<PatchBalancingInfo, Double> findFirstCandidateNotLossGraphCoherence(
      List<ImmutablePair<PatchBalancingInfo, Double>> orderCandidates) {
  }

  private List<PatchBalancingInfo> bindWitchStatistic(Set<Patch> candidatesToLoadBalancing,
      TransferDataHandler transferDataHandler) {

    return candidatesToLoadBalancing.parallelStream().map(patch -> {
      List<PatchId> newNeighbours =
          findNeighbouringPatches(patch.getPatchId(), transferDataHandler);
      List<PatchId> removedShadowPatches =
          findShadowPatchesNeighbouringOnlyWithPatch(patch.getPatchId(), transferDataHandler);

      return PatchBalancingInfo.builder()
          .patchId(patch.getPatchId())
          .countOfVehicle(countCars(patch))
          .newBorderPatchesAfterTransfer(newNeighbours)
          .countCarsInNewBorderPatches(countCars(newNeighbours, transferDataHandler))
          .shadowPatchesToRemoveAfterTransfer(removedShadowPatches)
          .countCarsInRemovedShadowPatches(countCars(removedShadowPatches, transferDataHandler))
          .build();
    }).toList();
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
