package pl.edu.agh.hiputs.loadbalancer;

import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.LoadInfo;
import static pl.edu.agh.hiputs.loadbalancer.PID.PID.INITIALIZATION_STEP;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.messages.LoadInfoMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.loadbalancer.PID.PID;
import pl.edu.agh.hiputs.loadbalancer.PID.ZieglerNicholsAutoTuner;
import pl.edu.agh.hiputs.loadbalancer.model.BalancingMode;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.loadbalancer.utils.MapFragmentCostCalculatorUtil;
import pl.edu.agh.hiputs.loadbalancer.utils.TimeToCarCostUtil;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PidLoadBalancingService implements LoadBalancingStrategy, Subscriber {

  private final ConfigurationService configurationService;
  private final SubscriptionService subscriptionService;

  private final SimulationStatisticService simulationStatisticService;

  private final LocalLoadStatisticService localLoadStatisticService;

  private final SimplyLoadBalancingService simplyLoadBalancingService;
  private final TicketService ticketService;

  private final Map<MapFragmentId, LoadBalancingHistoryInfo> loadRepository = new HashMap<>();

  private static final int MAX_AGE_DIFF = 5;
  private static final double ALLOW_LOAD_IMBALANCE = 0.01;

  // private PID timePID;
  private PID carPID;

  private int step = 0;

  @PostConstruct
  void init() {
    if (configurationService.getConfiguration().getBalancingMode() == BalancingMode.PID) {
      subscriptionService.subscribe(this, LoadInfo);
    }
  }

  @Override
  public LoadBalancingDecision makeBalancingDecision(TransferDataHandler transferDataHandler) {

    if(step < INITIALIZATION_STEP + 1) { // first step we use  SIMPLY algorithm
      step++;
      return simplyLoadBalancingService.makeBalancingDecision(transferDataHandler);
    }

    LoadBalancingDecision loadBalancingDecision = new LoadBalancingDecision();
    loadBalancingDecision.setAge(step);
    LoadBalancingHistoryInfo info = localLoadStatisticService.getMyLastLoad();

    calculateNewTargetAndInitPID(info);

    double myCost = MapFragmentCostCalculatorUtil.calculateCost(info);

    double timeBalanceTarget = carPID.nextValue(info.getTimeCost());
    // double timeBalanceTarget = timePID.nextValue(info.getTimeCost());
    log.debug("My cost {}, carTarget {}, time target {}", myCost, timeBalanceTarget, 0);

    simulationStatisticService.saveLoadBalancingStatistic(info.getTimeCost(), info.getCarCost(), myCost, step, info.getWaitingTime());
    step++;

    if (timeBalanceTarget > 0 || transferDataHandler.getLocalPatchesSize() < 5 ) {
      loadBalancingDecision.setLoadBalancingRecommended(false);
      return loadBalancingDecision;
    }

    timeBalanceTarget = Math.abs(timeBalanceTarget);
    // timeBalanceTarget = Math.abs(timeBalanceTarget);
    log.debug("Car imbalance: {}, time imbalance: {}",timeBalanceTarget / carPID.getTarget() <= ALLOW_LOAD_IMBALANCE, 0);

    if (timeBalanceTarget <= 10) {
      loadBalancingDecision.setLoadBalancingRecommended(false);
      return loadBalancingDecision;
    }

    loadBalancingDecision.setLoadBalancingRecommended(true);
    loadBalancingDecision.setExtremelyLoadBalancing(true);
    ImmutablePair<MapFragmentId, Double> candidate = selectNeighbourToBalancing(transferDataHandler);

    if(candidate == null){
      loadBalancingDecision.setLoadBalancingRecommended(false);
      return loadBalancingDecision;
    }

    loadBalancingDecision.setSelectedNeighbour(candidate.getLeft());
    loadBalancingDecision.setCarImbalanceRate(TimeToCarCostUtil.getCarsToTransferByDiff(info, loadRepository.get(candidate.getLeft()), timeBalanceTarget));
    loadBalancingDecision.setCarImbalanceRate((long) timeBalanceTarget);
    return loadBalancingDecision;
  }

  private void calculateNewTargetAndInitPID(LoadBalancingHistoryInfo info) {
    // double timeAvg = loadRepository.values()
    //     .stream()
    //     .filter(i -> i.getAge() + MAX_AGE_DIFF >= step)
    //     .mapToDouble(LoadBalancingHistoryInfo::getTimeCost)
    //     .average()
    //     .orElse(0.f);

    double carAvg = loadRepository.values()
        .stream()
        .filter(i -> i.getAge() + MAX_AGE_DIFF >= step)
        .mapToDouble(LoadBalancingHistoryInfo::getCarCost)
        .average()
        .orElse(0.f);

    log.debug("Car target {} time target {}", carAvg, 0);

    if(carPID == null){
      // timePID = new PID(new ZieglerNicholsAutoTuner(), timeAvg, -10000d, 10000d);
      carPID = new PID(new ZieglerNicholsAutoTuner(), carAvg, -300d, 0);
    } else {
      // timePID.setTarget(timeAvg);
      carPID.setTarget(carAvg);
    }

  }

  private ImmutablePair<MapFragmentId, Double> selectNeighbourToBalancing(TransferDataHandler transferDataHandler) {
    ticketService.setActualStep(step);
    MapFragmentId candidate = ticketService.getActualTalker();

    if (candidate != null && hasActualCostInfo(candidate) && !transferDataHandler.getBorderPatches()
        .get(candidate)
        .isEmpty()) {
      return new ImmutablePair<MapFragmentId, Double>(candidate, calculateCost(candidate));
    }

    return null;
  }

  private boolean hasActualCostInfo(MapFragmentId mapFragmentId) {
    LoadBalancingHistoryInfo loadBalancingHistoryInfo = loadRepository.get(mapFragmentId);

    return loadBalancingHistoryInfo != null && loadBalancingHistoryInfo.getAge() + MAX_AGE_DIFF >= step;
  }

  private double calculateCost(MapFragmentId id) {
    LoadBalancingHistoryInfo info = loadRepository.get(id);
    return MapFragmentCostCalculatorUtil.calculateCost(info);
  }

  @Override
  public void notify(Message message) {

    addToLoadBalancing((LoadInfoMessage) message);

    if(step < INITIALIZATION_STEP + 1) {
      simplyLoadBalancingService.notify(message);
    }
  }

  private void addToLoadBalancing(LoadInfoMessage message) {
    MapFragmentId id = new MapFragmentId(message.getMapFragmentId());
    loadRepository.put(id, new LoadBalancingHistoryInfo(message.getCarCost(), message.getTime(), 0L, step));
  }
}
