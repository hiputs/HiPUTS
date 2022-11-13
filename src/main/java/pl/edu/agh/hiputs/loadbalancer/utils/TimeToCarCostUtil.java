package pl.edu.agh.hiputs.loadbalancer.utils;

import lombok.experimental.UtilityClass;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;

@UtilityClass
public class TimeToCarCostUtil {

  public static int getCarsToTransfer(LoadBalancingHistoryInfo meInfo, LoadBalancingHistoryInfo candidateInfo) {
    double mySingleCarTime = meInfo.getTimeCost() / Math.max(meInfo.getCarCost(), 1.0);
    double candidateSingleCarTime = candidateInfo.getTimeCost() / Math.max(candidateInfo.getCarCost(), 1.0);

    long timeDiff = Math.abs(candidateInfo.getTimeCost() - meInfo.getTimeCost());
    double carAvgCost = (mySingleCarTime + candidateSingleCarTime) / 2.0;

    return (int) (timeDiff / carAvgCost / 2);
  }

  public static long getCarsToTransferByDiff(LoadBalancingHistoryInfo meInfo, LoadBalancingHistoryInfo candidateInfo, double timeBalanceTarget) {
    double mySingleCarTime = meInfo.getTimeCost() / Math.max(meInfo.getCarCost(), 1.0);
    double candidateSingleCarTime = candidateInfo.getTimeCost() / Math.max(candidateInfo.getCarCost(), 1.0);

    double carAvgCost = (mySingleCarTime + candidateSingleCarTime) / 2.0;

    return (int) (timeBalanceTarget / carAvgCost / 2);
  }
}
