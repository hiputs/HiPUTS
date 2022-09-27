package pl.edu.agh.hiputs.model.car.driver.deciders.junction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AccelerationDecisionResult {
  private double acceleration;
  private boolean isLocked;
}
