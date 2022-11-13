package pl.edu.agh.hiputs.loadbalancer.utils;

import lombok.experimental.UtilityClass;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;

@UtilityClass
public class MapFragmentCostCalculatorUtil {

  private static final double CAR_DIVIDER = 0d;
  private static final double TIME_DIVIDER = 1d;

  public static double calculateCost(LoadBalancingHistoryInfo info) {
    // return info.getCarCost() / CAR_DIVIDER + info.getTimeCost() / TIME_DIVIDER;
    return info.getTimeCost();
  }
}
