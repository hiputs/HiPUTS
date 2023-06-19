package pl.edu.agh.hiputs.loadbalancer;

import static pl.edu.agh.hiputs.loadbalancer.utils.CarCounterUtil.countCars;
import static pl.edu.agh.hiputs.loadbalancer.utils.GraphCoherencyUtil.isCoherency;
import static pl.edu.agh.hiputs.loadbalancer.utils.PatchConnectionSearchUtil.findNeighbouringPatches;
import static pl.edu.agh.hiputs.loadbalancer.utils.PatchConnectionSearchUtil.findShadowPatchesNeighbouringOnlyWithPatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.LoadSynchronizationMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.SerializedPatchTransfer;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.loadbalancer.LoadBalancingStrategy.LoadBalancingDecision;
import pl.edu.agh.hiputs.loadbalancer.model.BalancingMode;
import pl.edu.agh.hiputs.loadbalancer.model.PatchBalancingInfo;
import pl.edu.agh.hiputs.loadbalancer.utils.PatchCostCalculatorUtil;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.PatchTransferService;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadBalancingServiceImpl implements LoadBalancingService, Subscriber {

  private static final int MAX_PATCH_EXCHANGE = 40;
  private final PatchTransferService patchTransferService;
  private final SimulationStatisticService simulationStatisticService;
  private final WorkerSubscriptionService subscriptionService;
  private final MessageSenderService messageSenderService;
  private final List<Message> synchronizationLoadBalancingList = new ArrayList<>();
  private final LocalLoadMonitorService localLoadMonitorService;
  private final SelectNeighbourToBalancingService selectNeighbourToBalancingService;

  private LoadBalancingStrategy strategy;
  private MapFragmentId lastLoadBalancingCandidate = null; //We must repete their neighbourTransferMessage
  private int actualStep = 0;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.LoadSynchronizationMessage);
    strategy = getStrategyByMode();
  }

  @Override
  public MapFragmentId startLoadBalancing(TransferDataHandler transferDataHandler) {
    if (ConfigurationService.getConfiguration().getWorkerCount() == 1) {
      return null;
    }

    List<MapFragmentId> neighboursToNotify = List.copyOf(transferDataHandler.getNeighbors());

    log.debug("neighboursToNotify {}", neighboursToNotify);
    balance(transferDataHandler);
    if (!ConfigurationService.getConfiguration().getBalancingMode().equals(BalancingMode.NONE)) {
      synchronizedWithNeighbour(neighboursToNotify);
    }
    log.debug("neighboursToNotify after balance {}", neighboursToNotify);

    actualStep++;
    return lastLoadBalancingCandidate;
  }

  private void balance(TransferDataHandler transferDataHandler) {
    LoadBalancingDecision loadBalancingDecision = strategy.makeBalancingDecision(transferDataHandler, actualStep);

    if (!loadBalancingDecision.isLoadBalancingRecommended()) {
      simulationStatisticService.saveLoadBalancingDecision(false, null, null, 0f, loadBalancingDecision.getAge());
      lastLoadBalancingCandidate = null;
      return;
    }

    log.info("Start loadbalancing worker id: {} with {}", transferDataHandler.getMe().getId(),
        loadBalancingDecision.getSelectedNeighbour().getId());
    MapFragmentId recipient = loadBalancingDecision.getSelectedNeighbour();
    lastLoadBalancingCandidate = recipient;
    long targetBalanceCars = loadBalancingDecision.getCarImbalanceRate();

    if (targetBalanceCars == 0) {
      return;
    }

    long transferCars = 0;
    List<SerializedPatchTransfer> serializedPatchTransfers = new ArrayList<>();
    transferDataHandler.clearMapOfSentPatches();

    do {
      ImmutablePair<PatchBalancingInfo, Double> patchInfo =
          findPatchesToSend(recipient, transferDataHandler, targetBalanceCars);

      if (patchInfo == null) {
        break;
      }

      simulationStatisticService.saveLoadBalancingDecision(true, patchInfo.getLeft().getPatchId().getValue(),
          recipient.getId(), patchInfo.getRight(), loadBalancingDecision.getAge());

      serializedPatchTransfers.add(
          patchTransferService.prepareSinglePatchItemAndNotifyNeighbour(recipient, patchInfo.getLeft().getPatchId(),
              transferDataHandler));

      transferDataHandler.updateMapOfSentPatches(patchInfo.getLeft().getPatchId(), recipient);
      transferCars += patchInfo.getLeft().getCountOfVehicle();
      log.debug(
          "LB loop - extremelyLB: {}, transferedCars: {}, targetCarsToTransfer: {}, number of patches to transfer: {},"
              + "number of left local patches: {}", loadBalancingDecision.isExtremelyLoadBalancing(), transferCars,
          targetBalanceCars, serializedPatchTransfers.size(), transferDataHandler.getLocalPatchesSize());
    } while (loadBalancingDecision.isExtremelyLoadBalancing() && transferCars <= targetBalanceCars * 0.9 && serializedPatchTransfers.size() < MAX_PATCH_EXCHANGE && transferDataHandler.getLocalPatchesSize() >= 5);

    patchTransferService.sendPatchMessage(recipient, serializedPatchTransfers);
  }

  private synchronized void synchronizedWithNeighbour(List<MapFragmentId> neighboursToNotify) {

    neighboursToNotify.forEach(id -> {
      try {
        messageSenderService.send(id, new LoadSynchronizationMessage());
      } catch (IOException e) {
        log.error("Error util send synchronization message", e);
      }
    });
    while (synchronizationLoadBalancingList.size() < neighboursToNotify.size()) {
      try {
        this.wait(10);
      } catch (InterruptedException e) {
        log.error("error until wait for loadbalancing synchronization", e);
      }
    }

    synchronizationLoadBalancingList.clear();
  }

  private ImmutablePair<PatchBalancingInfo, Double> findPatchesToSend(MapFragmentId recipient,
      TransferDataHandler transferDataHandler, final long carBalanceTarget) {

    Set<Patch> candidatesToLoadBalancing = transferDataHandler.getBorderPatches().get(recipient);

    if (candidatesToLoadBalancing == null) {
      return null;
    }
    List<PatchBalancingInfo> candidatesWithStatistic =
        bindWitchStatistic(candidatesToLoadBalancing, transferDataHandler);

    List<ImmutablePair<PatchBalancingInfo, Double>> orderCandidates = candidatesWithStatistic.stream()
        .map(i -> new ImmutablePair<PatchBalancingInfo, Double>(i,
            PatchCostCalculatorUtil.calculateCost(i, carBalanceTarget)))
        .sorted(Comparator.comparingDouble(ImmutablePair::getRight))
        .toList();

    if (orderCandidates.size() == 0) {
      return null;
    }

    ImmutablePair<PatchBalancingInfo, Double> selectedCandidate =
        findFirstCandidateNotLossGraphCoherence(orderCandidates, transferDataHandler, recipient);

    log.debug("Select candidate id {} with cost {}", selectedCandidate.getLeft().getPatchId().getValue(),
        selectedCandidate.getRight());

    return selectedCandidate;
  }

  private ImmutablePair<PatchBalancingInfo, Double> findFirstCandidateNotLossGraphCoherence(
      List<ImmutablePair<PatchBalancingInfo, Double>> orderCandidates, TransferDataHandler transferDataHandler,
      MapFragmentId targetMapFragmentId) {

    int attempt = 0;
    for (final ImmutablePair<PatchBalancingInfo, Double> orderCandidate : orderCandidates) {
      if (isCoherency(transferDataHandler, orderCandidate.getLeft().getPatchId(), targetMapFragmentId)) {
        return orderCandidate;
      }

      if (attempt++ == 5) {
        return orderCandidates.get(0);
      }
    }

    return orderCandidates.get(0);
  }

  private List<PatchBalancingInfo> bindWitchStatistic(Set<Patch> candidatesToLoadBalancing,
      TransferDataHandler transferDataHandler) {

    return candidatesToLoadBalancing.parallelStream().map(patch -> {
      List<PatchId> newNeighbours = findNeighbouringPatches(patch.getPatchId(), transferDataHandler);
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

  private LoadBalancingStrategy getStrategyByMode() {
    return switch (ConfigurationService.getConfiguration().getBalancingMode()) {
      case SIMPLY -> new SimplyLoadBalancingStrategy(simulationStatisticService, localLoadMonitorService,
          selectNeighbourToBalancingService);
      case PID -> new PidLoadBalancingStrategy(simulationStatisticService, localLoadMonitorService,
          new SimplyLoadBalancingStrategy(simulationStatisticService, localLoadMonitorService,
              selectNeighbourToBalancingService), selectNeighbourToBalancingService);
      default -> new NoneLoadBalancingStrategy(simulationStatisticService, localLoadMonitorService);
    };
  }

  @Override
  public synchronized void notify(Message message) {
    synchronizationLoadBalancingList.add(message);
    this.notifyAll();
  }
}
