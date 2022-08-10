package pl.edu.agh.hiputs.loadbalancer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class LoadBalancingHistoryInfo {
  private long carCost;
  private long timeCost;
  private int age;
}
