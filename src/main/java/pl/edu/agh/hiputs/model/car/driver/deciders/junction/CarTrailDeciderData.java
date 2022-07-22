package pl.edu.agh.hiputs.model.car.driver.deciders.junction;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Value;
import pl.edu.agh.hiputs.model.id.LaneId;

@Value
@AllArgsConstructor
public class CarTrailDeciderData {
  private double speed;
  private double distance;
  private double length;
  private double acceleration;
  private double maxSpeed;
  private Optional<LaneId> outgoingLaneIdOptional;
}

