package pl.edu.agh.hiputs.model.car.driver.deciders;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;

@Value
@AllArgsConstructor
@Builder
public class CarFollowingEnvironment {

  double distance;
  CarReadable thisCar = null;
  Optional<CarReadable> followingCar;
  Optional<JunctionId> prevCrossroadId;
  Optional<RoadId> incomingRoadId;
  Optional<LaneId> incomingLaneId;

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(distance=" + distance + ", followingCar=Optional" + (followingCar.map(
        carReadable -> "[" + carReadable.getCarId() + "]").orElse(".empty")) + ", prevCrossroadId=Optional"
        + (prevCrossroadId.map(junctionId -> "[" + junctionId + "]").orElse(".empty")) + ", incomingRoadId=Optional"
        + (incomingRoadId.map(roadId -> "[" + roadId + "]").orElse(".empty)")) + ", incomingLaneId=Optional"
        + (incomingLaneId.map(laneId -> "[" + laneId + "]").orElse(".empty)"));
  }
}
