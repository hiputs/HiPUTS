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

  private final Map<MapFragmentId, LoadBalancingHistoryInfo> loadRepository = new HashMap<>();

  private static final int MAX_AGE_DIFF = 5;
  private static final double ALLOW_LOAD_IMBALANCE = 0.03;
  private static final double LOW_THRESHOLD = 0.1;

  private PID timePID;
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
    LoadBalancingDecision loadBalancingDecision = new LoadBalancingDecision();
    loadBalancingDecision.setAge(step);
    LoadBalancingHistoryInfo info = localLoadStatisticService.getMyLastLoad();

    calculateNewTargetAndInitPID(info);

    double myCost = MapFragmentCostCalculatorUtil.calculateCost(info);

    double carBalanceTarget = carPID.nextValue(info.getCarCost());
    double timeBalanceTarget = timePID.nextValue(info.getTimeCost());
    log.info("My cost {}, carTarget {}, time target {}", myCost, carBalanceTarget, timeBalanceTarget);

    if(step < INITIALIZATION_STEP + 1) { // first step we use  SIMPLY algorithm
      step++;
      return simplyLoadBalancingService.makeBalancingDecision(transferDataHandler);
    }

    simulationStatisticService.saveLoadBalancingStatistic(info.getTimeCost(), info.getCarCost(), myCost, step, info.getWaitingTime());
    step++;

    if (carBalanceTarget < 0 || timeBalanceTarget > 0) {
      loadBalancingDecision.setLoadBalancingRecommended(false);
      return loadBalancingDecision;
    }

    carBalanceTarget = Math.abs(carBalanceTarget);
    timeBalanceTarget = Math.abs(timeBalanceTarget);
    log.info("Car imbalance: {}, time imbalance: {}",carBalanceTarget / carPID.getTarget() <= ALLOW_LOAD_IMBALANCE, timeBalanceTarget / timePID.getTarget() <= ALLOW_LOAD_IMBALANCE);

    if (carBalanceTarget / carPID.getTarget() <= ALLOW_LOAD_IMBALANCE
        && timeBalanceTarget / timePID.getTarget() <= ALLOW_LOAD_IMBALANCE) {
      loadBalancingDecision.setLoadBalancingRecommended(false);
      return loadBalancingDecision;
    }

    if(carBalanceTarget / carPID.getTarget() > LOW_THRESHOLD
        && timeBalanceTarget / timePID.getTarget() > LOW_THRESHOLD){
      loadBalancingDecision.setExtremelyLoadBalancing(true);
    }

    loadBalancingDecision.setLoadBalancingRecommended(true);
    ImmutablePair<MapFragmentId, Double> candidate = selectNeighbourToBalancing(transferDataHandler);

    loadBalancingDecision.setSelectedNeighbour(candidate.getLeft());
    loadBalancingDecision.setCarImbalanceRate((long) carBalanceTarget);
    return loadBalancingDecision;
  }

  private void calculateNewTargetAndInitPID(LoadBalancingHistoryInfo info) {
    double timeAvg = loadRepository.values()
        .stream()
        .filter(i -> i.getAge() + MAX_AGE_DIFF >= step)
        .mapToDouble(LoadBalancingHistoryInfo::getTimeCost)
        .average()
        .orElse(0.f);

    double carAvg = loadRepository.values()
        .stream()
        .filter(i -> i.getAge() + MAX_AGE_DIFF >= step)
        .mapToDouble(LoadBalancingHistoryInfo::getCarCost)
        .average()
        .orElse(0.f);

    log.info("Car target {} time target {}", timeAvg, carAvg);

    if(timePID == null || carPID == null){
      timePID = new PID(new ZieglerNicholsAutoTuner(), timeAvg);
      carPID = new PID(new ZieglerNicholsAutoTuner(), carAvg);
    } else {
      timePID.setTarget(timeAvg);
      carPID.setTarget(carAvg);
    }

  }

  private ImmutablePair<MapFragmentId, Double> selectNeighbourToBalancing(TransferDataHandler transferDataHandler) {

    return transferDataHandler.getNeighbors()
        .stream()
        .filter(this::hasActualCostInfo)
        .map(id -> new ImmutablePair<MapFragmentId, Double>(id, calculateCost(id)))
        .min(Comparator.comparingDouble(ImmutablePair::getRight))
        .orElse(null);
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
