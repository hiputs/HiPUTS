package pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LaneChangeDecision {

  private final double acceleration;
  private final LaneChange laneChange;

}
