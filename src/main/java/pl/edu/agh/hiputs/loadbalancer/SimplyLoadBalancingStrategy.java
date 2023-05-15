package pl.edu.agh.hiputs.loadbalancer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.loadbalancer.utils.MapFragmentCostCalculatorUtil;
import pl.edu.agh.hiputs.loadbalancer.utils.TimeToCarCostUtil;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;

@Slf4j
@RequiredArgsConstructor
public class SimplyLoadBalancingStrategy implements LoadBalancingStrategy {

  private static final double ALLOW_LOAD_IMBALANCE = 1.05;
  private static final double LOW_THRESHOLD = 1.25;
  private final SimulationStatisticService simulationStatisticService;

  private final LocalLoadMonitorService localLoadMonitorService;
  private final SelectNeighbourToBalancingService selectNeighbourToBalancingService;

  @Override
  public LoadBalancingDecision makeBalancingDecision(TransferDataHandler transferDataHandler, int actualStep) {
    LoadBalancingDecision loadBalancingDecision = new LoadBalancingDecision();
    int age = actualStep;
    loadBalancingDecision.setAge(age);

    LoadBalancingHistoryInfo info = localLoadMonitorService.getMyLastLoad(actualStep - 1);
    double myCost = MapFragmentCostCalculatorUtil.calculateCost(info);

    simulationStatisticService.saveLoadBalancingStatistic(info.getTimeCost(), info.getCarCost(), myCost, age - 1,
        info.getWaitingTime());
    age++;

    if (transferDataHandler.getNeighbors().isEmpty() || age < 3 || transferDataHandler.getLocalPatchesSize() < 5) {
      loadBalancingDecision.setLoadBalancingRecommended(false);
      return loadBalancingDecision;
    }

    try {
      ImmutablePair<MapFragmentId, Double> candidate = selectNeighbourToBalancingService.selectNeighbourToBalancing(transferDataHandler,
          age);

      if (candidate == null) {
        loadBalancingDecision.setLoadBalancingRecommended(false);
        return loadBalancingDecision;
      }
      boolean shouldBalancing = myCost / candidate.getRight() > ALLOW_LOAD_IMBALANCE;
      boolean shouldExtremeBalancing = myCost / candidate.getRight() >  LOW_THRESHOLD;

      log.info("should {}, mycost {} candidate cost {}", shouldBalancing, myCost, candidate.getRight());

      loadBalancingDecision.setLoadBalancingRecommended(shouldBalancing);
      loadBalancingDecision.setExtremelyLoadBalancing(shouldExtremeBalancing);

      if (!shouldBalancing) {
        return loadBalancingDecision;
      }

      loadBalancingDecision.setSelectedNeighbour(candidate.getLeft());
      loadBalancingDecision.setCarImbalanceRate(TimeToCarCostUtil.getCarsToTransfer(info, selectNeighbourToBalancingService.getLoadRepository().get(candidate.getLeft())));
    } catch (Exception e) {
      log.error("Could not make decision", e);
      loadBalancingDecision.setLoadBalancingRecommended(false);
    }
    return loadBalancingDecision;
  }
}
