package pl.edu.agh.hiputs.loadbalancer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoadBalancingHistoryInfo {
  private int carCost;
  private long timeCost;
  private int age;
}
