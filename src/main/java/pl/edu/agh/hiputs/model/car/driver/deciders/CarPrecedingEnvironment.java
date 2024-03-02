package pl.edu.agh.hiputs.model.car.driver.deciders;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Value;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;

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
public class CarPrecedingEnvironment {
  double distance;
  Optional<CarReadable> precedingCar;
  Optional<JunctionId> nextCrossroadId;
  Optional<RoadId> incomingRoadId;
  Optional<LaneId> incomingLaneId;

  public CarPrecedingEnvironment(Optional<CarReadable> precedingCar, Optional<JunctionId> nextCrossroadId,
      double distance) {
    this.distance = distance;
    this.precedingCar = precedingCar;
    this.nextCrossroadId = nextCrossroadId;
    this.incomingRoadId = Optional.empty();
    this.incomingLaneId = Optional.empty();
  }

  @Override
  public String toString(){
    return getClass().getSimpleName() + "(distance=" + distance
        + ", precedingCar=Optional" + (precedingCar.map(carReadable -> "[" + carReadable.getCarId() + "]")
        .orElse(".empty"))
        + ", nextCrossroadId=Optional" + (nextCrossroadId.map(junctionId -> "[" + junctionId + "]").orElse(".empty"))
        + ", incomingRoadId=Optional" + (incomingRoadId.map(roadId -> "[" + roadId + "]").orElse(".empty)"))
        + ", incomingLaneId=Optional" + (incomingLaneId.map(laneId -> "[" + laneId + "]").orElse(".empty)"));
  }
}
