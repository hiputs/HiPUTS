package pl.edu.agh.hiputs.loadbalancer.utils;

import lombok.experimental.UtilityClass;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;

@UtilityClass
public class MapFragmentCostCalculatorUtil {

  public static int calculateCost(LoadBalancingHistoryInfo info) {
    info.getCarCost() + info.getTimeCost()
  }
}
