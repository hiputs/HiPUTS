package pl.edu.agh.hiputs.loadbalancer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.loadbalancer.utils.MapFragmentCostCalculatorUtil;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;

@Service
@RequiredArgsConstructor
public class NoneLoadBalancingStrategy implements LoadBalancingStrategy{

  private final SimulationStatisticService simulationStatisticService;

  private final LocalLoadStatisticService localLoadStatisticService;

  int age = 0;
  @Override
  public LoadBalancingDecision makeBalancingDecision(TransferDataHandler transferDataHandler) {
    LoadBalancingDecision loadBalancingDecision = new LoadBalancingDecision();
    loadBalancingDecision.setAge(age);
    loadBalancingDecision.setLoadBalancingRecommended(false);

    LoadBalancingHistoryInfo info = localLoadStatisticService.getMyLastLoad();
    double myCost = MapFragmentCostCalculatorUtil.calculateCost(info);

    simulationStatisticService.saveLoadBalancingStatistic(info.getTimeCost(), info.getCarCost(), myCost, age, info.getWaitingTime());
    age++;

    return loadBalancingDecision;
  }
}
