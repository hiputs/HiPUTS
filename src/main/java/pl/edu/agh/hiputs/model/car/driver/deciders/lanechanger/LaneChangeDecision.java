package pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.model.id.LaneId;

@Getter
@AllArgsConstructor
public class LaneChangeDecision {

  private final double acceleration;
  private final LaneId targetLaneId;

}
