package pl.edu.agh.hiputs.loadbalancer;

import static pl.edu.agh.hiputs.loadbalancer.utils.CarCounterUtil.countCars;
import static pl.edu.agh.hiputs.loadbalancer.utils.GraphCoherencyUtil.isCoherency;
import static pl.edu.agh.hiputs.loadbalancer.utils.PatchConnectionSearchUtil.findNeighbouringPatches;
import static pl.edu.agh.hiputs.loadbalancer.utils.PatchConnectionSearchUtil.findShadowPatchesNeighbouringOnlyWithPatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferNotificationMessage;
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
  private final AtomicInteger synchronizationLoadBalancingCounter = new AtomicInteger();
  private final LocalLoadMonitorService localLoadMonitorService;
  private final SelectNeighbourToBalancingService selectNeighbourToBalancingService;
  private final Map<MapFragmentId, PatchTransferNotificationMessage> notifications = new HashMap<>();

  private LoadBalancingStrategy strategy;
  private MapFragmentId lastLoadBalancingCandidate = null; //We must repete their neighbourTransferMessage
  private int actualStep = 0;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferNotificationMessage);
    strategy = getStrategyByMode();
  }

  @Override
  public MapFragmentId startLoadBalancing(TransferDataHandler transferDataHandler) {
    if (ConfigurationService.getConfiguration().getWorkerCount() == 1) {
      return null;
    }
    transferDataHandler.clearMapOfSentPatches();
    log.debug("MapOfSentPatches content: {}",
        transferDataHandler.getMapOfSentPatches().entrySet().stream().map(a -> a.getKey() + ":" + a.getValue()));

    notifications.clear();
    transferDataHandler.getNeighbors()
        .forEach(neigh -> notifications.put(neigh, PatchTransferNotificationMessage.builder()
            .senderId(transferDataHandler.getMe().getId())
            .receiverId(null)
            .transferredPatchesList(new LinkedList<>())
            .connectionDto(null)
            .build()));
    log.debug("neighbours: {}, {}", transferDataHandler.getNeighbors().size(), transferDataHandler.getNeighbors());
    log.debug("neighboursToNotify {}", notifications.keySet());
    balance(transferDataHandler);
    log.debug("after balance {} ", notifications.keySet());
    if (!ConfigurationService.getConfiguration().getBalancingMode().equals(BalancingMode.NONE)) {
      sendNotificationsAndSynchronizeWithNeighbours();
    }

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
    notifications.forEach((k, v) -> {
      if (!k.equals(recipient)) { // for each neighbour besides the one chosed for LB
        v.setConnectionDto(messageSenderService.getConnectionDtoMap().get(recipient));
        v.setReceiverId(recipient.getId());
      }
    });

    do {
      ImmutablePair<PatchBalancingInfo, Double> candidatePatchInfo =
          findPatchesToSend(recipient, transferDataHandler, targetBalanceCars);

      if (candidatePatchInfo == null) {
        break;
      }

      simulationStatisticService.saveLoadBalancingDecision(true, candidatePatchInfo.getLeft().getPatchId().getValue(),
          recipient.getId(), candidatePatchInfo.getRight(), loadBalancingDecision.getAge());

      patchTransferService.neighboursToNotify(recipient, candidatePatchInfo.getLeft().getPatchId(), transferDataHandler)
          .forEach(key -> {
            notifications.get(key)
                .getTransferredPatchesList()
                .add(candidatePatchInfo.getLeft().getPatchId().getValue());
            log.debug("Notification to {} added to map, patch {}, notifications{}", key,
                candidatePatchInfo.getLeft().getPatchId(), notifications.get(key).getTransferredPatchesList());
          });

      serializedPatchTransfers.add(
          patchTransferService.prepareSinglePatchItem(recipient, candidatePatchInfo.getLeft().getPatchId(),
              transferDataHandler));

      transferDataHandler.updateMapOfSentPatches(candidatePatchInfo.getLeft().getPatchId(), recipient);
      transferCars += candidatePatchInfo.getLeft().getCountOfVehicle();
      log.debug(
          "LB loop - extremelyLB: {}, transferedCars: {}, targetCarsToTransfer: {}, number of patches to transfer: {},"
              + "number of left local patches: {}", loadBalancingDecision.isExtremelyLoadBalancing(), transferCars,
          targetBalanceCars, serializedPatchTransfers.size(), transferDataHandler.getLocalPatchesSize());
    } while (loadBalancingDecision.isExtremelyLoadBalancing() && transferCars <= targetBalanceCars * 0.9
        && serializedPatchTransfers.size() < MAX_PATCH_EXCHANGE && transferDataHandler.getLocalPatchesSize() >= 5);

    log.debug("Recipient {}, from {}", recipient.getId(), transferDataHandler.getMe().getId());
    log.debug("MapOfSentPatches content: {}",
        transferDataHandler.getMapOfSentPatches().entrySet().stream().map(a -> a.getKey() + ":" + a.getValue()));
    patchTransferService.sendPatchMessage(transferDataHandler.getMe(), recipient, serializedPatchTransfers);
  }

  private synchronized void sendNotificationsAndSynchronizeWithNeighbours() {
    // sends messages to all neighbours - 1st purpose: synchronization after LB (sends empty msg when no notification
    // should be )
    // 2nd purpose - notify neighbours about changes in their shadow patches

    notifications.forEach((mapId, msg) -> {
      try {
        messageSenderService.send(mapId, msg);
        log.debug("Notification to {} - transferred patches {} ", mapId.getId(), msg.getTransferredPatchesList());
      } catch (IOException e) {
        log.error("Error util send synchronization message", e);
      }
    });
    while (synchronizationLoadBalancingCounter.get() < notifications.keySet().size()) {
      try {
        this.wait(10);
      } catch (InterruptedException e) {
        log.error("error until wait for loadbalancing synchronization", e);
      }
    }
    synchronizationLoadBalancingCounter.set(0);
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
      log.debug("Patch candidate {} is not coherent", orderCandidate.getLeft().getPatchId());

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
    synchronizationLoadBalancingCounter.incrementAndGet();
    this.notifyAll();
  }
}
