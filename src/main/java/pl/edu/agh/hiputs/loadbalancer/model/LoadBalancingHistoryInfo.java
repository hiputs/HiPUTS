package pl.edu.agh.hiputs.loadbalancer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;

@AllArgsConstructor
@Builder
@Getter
public class LoadBalancingHistoryInfo {
  private long carCost;
  private long timeCost;
  private long waitingTime;
  private final int age;
}
