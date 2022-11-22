package pl.edu.agh.hiputs.loadbalancer;

import static pl.edu.agh.hiputs.loadbalancer.PID.PID.INITIALIZATION_STEP;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.loadbalancer.PID.PID;
import pl.edu.agh.hiputs.loadbalancer.PID.ZieglerNicholsAutoTuner;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.loadbalancer.utils.MapFragmentCostCalculatorUtil;
import pl.edu.agh.hiputs.loadbalancer.utils.TimeToCarCostUtil;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PidLoadBalancingService implements LoadBalancingStrategy {
  private final SimulationStatisticService simulationStatisticService;

  private final LocalLoadStatisticService localLoadStatisticService;

  private final SimplyLoadBalancingService simplyLoadBalancingService;

  private final SelectNeighbourToBalancingService selectNeighbourToBalancingService;

  private static final int MAX_AGE_DIFF = 5;
  private static final double ALLOW_LOAD_IMBALANCE = 0.02;

  // private PID timePID;
  private PID carPID;

  private int step = 0;

  @Override
  public LoadBalancingDecision makeBalancingDecision(TransferDataHandler transferDataHandler, int actualStep) {
    step = actualStep;
    if(step < INITIALIZATION_STEP) { // first step we use  SIMPLY algorithm
      return simplyLoadBalancingService.makeBalancingDecision(transferDataHandler, actualStep);
    }

    LoadBalancingDecision loadBalancingDecision = new LoadBalancingDecision();
    loadBalancingDecision.setAge(step);
    LoadBalancingHistoryInfo info = localLoadStatisticService.getMyLastLoad();

    ImmutablePair<MapFragmentId, Double> candidate = selectNeighbourToBalancingService.selectNeighbourToBalancing(transferDataHandler, step);
    calculateNewTargetAndInitPID(info);

    double myCost = MapFragmentCostCalculatorUtil.calculateCost(info);

    double balanceTarget = carPID.nextValue(myCost);
    // double timeBalanceTarget = timePID.nextValue(info.getTimeCost());
    log.info("My cost {}, carTarget {}, time target {}", myCost, balanceTarget, 0);

    simulationStatisticService.saveLoadBalancingStatistic(info.getTimeCost(), info.getCarCost(), myCost, step, info.getWaitingTime());

    if (balanceTarget >= 0 || transferDataHandler.getLocalPatchesSize() < 5 ) {
      loadBalancingDecision.setLoadBalancingRecommended(false);
      return loadBalancingDecision;
    }

    balanceTarget = Math.abs(balanceTarget);
    // timeBalanceTarget = Math.abs(timeBalanceTarget);
    log.info("Car imbalance: {}, time imbalance: {}",balanceTarget / carPID.getTarget() <= ALLOW_LOAD_IMBALANCE, 0);

    if (balanceTarget / carPID.getTarget() <= ALLOW_LOAD_IMBALANCE) {
      loadBalancingDecision.setLoadBalancingRecommended(false);
      return loadBalancingDecision;
    }

    loadBalancingDecision.setLoadBalancingRecommended(true);
    loadBalancingDecision.setExtremelyLoadBalancing(true);

    if(candidate == null){
      loadBalancingDecision.setLoadBalancingRecommended(false);
      return loadBalancingDecision;
    }

    loadBalancingDecision.setSelectedNeighbour(candidate.getLeft());
    loadBalancingDecision.setCarImbalanceRate((long) (balanceTarget / (info.getMapCost() + selectNeighbourToBalancingService.getLoadRepository().get(candidate.getLeft()).getMapCost())) / 2);
    return loadBalancingDecision;
  }

  private void calculateNewTargetAndInitPID(LoadBalancingHistoryInfo info) {
    // double timeAvg = loadRepository.values()
    //     .stream()
    //     .filter(i -> i.getAge() + MAX_AGE_DIFF >= step)
    //     .mapToDouble(LoadBalancingHistoryInfo::getTimeCost)
    //     .average()
    //     .orElse(0.f);

    List<LoadBalancingHistoryInfo> values = new LinkedList<>(selectNeighbourToBalancingService.getLoadRepository().values());
    values.add(info);

    double totalMapCostAvg = values
        .stream()
        .filter(i -> i.getAge() + MAX_AGE_DIFF >= step)
        .mapToDouble(MapFragmentCostCalculatorUtil::calculateCost)
        .average()
        .orElse(0.f);

    log.info("Car target {} time target {}", totalMapCostAvg, 0);

    if(carPID == null){
      // timePID = new PID(new ZieglerNicholsAutoTuner(), timeAvg, -10000d, 10000d);
      carPID = new PID(new ZieglerNicholsAutoTuner(), totalMapCostAvg, -10000d, 0);
    } else {
      // timePID.setTarget(timeAvg);
      carPID.setTarget(totalMapCostAvg);
    }

  }
}
