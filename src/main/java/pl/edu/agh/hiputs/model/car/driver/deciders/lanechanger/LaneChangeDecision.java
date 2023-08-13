package pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.model.id.LaneId;

@Getter
@AllArgsConstructor
public class LaneChangeDecision {

  private final Optional<Double> acceleration;
  private final LaneId targetLaneId;

}
