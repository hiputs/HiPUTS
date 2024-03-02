package pl.edu.agh.hiputs.loadbalancer;

import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.loadbalancer.utils.MapFragmentCostCalculatorUtil;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;

@RequiredArgsConstructor
public class NoneLoadBalancingStrategy implements LoadBalancingStrategy{

  private final SimulationStatisticService simulationStatisticService;
  private final LocalLoadMonitorService localLoadMonitorService;


  @Override
  public LoadBalancingDecision makeBalancingDecision(TransferDataHandler transferDataHandler, int actualStep) {
    LoadBalancingDecision loadBalancingDecision = new LoadBalancingDecision();
    loadBalancingDecision.setAge(actualStep);
    loadBalancingDecision.setLoadBalancingRecommended(false);

    LoadBalancingHistoryInfo info = localLoadMonitorService.getMyLastLoad(actualStep);
    double myCost = MapFragmentCostCalculatorUtil.calculateCost(info);

    simulationStatisticService.saveLoadBalancingStatistic(info.getTimeCost(), info.getCarCost(), myCost, actualStep,
        info.getWaitingTime());

    return loadBalancingDecision;
  }
}
