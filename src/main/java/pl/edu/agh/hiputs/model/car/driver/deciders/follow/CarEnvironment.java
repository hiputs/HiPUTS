package pl.edu.agh.hiputs.model.car.driver.deciders.follow;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Value;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

/**
 * precedingCar contain preceding car if found
 * nextCrossroadId contain crossroadId if:
 * - precedingCar is empty and searching not exceeds car's route
 * - precedingCar is found on lane ending with crossroad
 * distance contain distance to car (if precedingCar is present, it takes into account length of precedingCar),
 * or to crossroad (if nextCrossroadId is present),
 * or if both Optionals are empty, distance to last lane on car's route
 */
@Value
@AllArgsConstructor
public class CarEnvironment {
  double distance;
  Optional<CarReadable> precedingCar;
  Optional<JunctionId> nextCrossroadId;
  Optional<LaneId> incomingLaneId;

  public CarEnvironment(Optional<CarReadable> precedingCar, Optional<JunctionId> nextCrossroadId, double distance) {
    this.distance = distance;
    this.precedingCar = precedingCar;
    this.nextCrossroadId = nextCrossroadId;
    this.incomingLaneId = Optional.empty();
  }
}
