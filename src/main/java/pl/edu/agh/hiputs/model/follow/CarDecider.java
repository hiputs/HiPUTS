package pl.edu.agh.hiputs.model.follow;

import java.util.Optional;
import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

public class CarDecider {

  IdmDecider idmDecider = new IdmDecider();
  BasicJunctionDecider junctionDecider = new BasicJunctionDecider();

  public double makeDecision(CarReadable car, RoadStructureReader roadStructureReader){
    CarEnvironment environment = getPrecedingCarOrCrossroad(car, roadStructureReader);
    if(!environment.getPrecedingCar().isEmpty()){
      return idmDecider.makeDecision(car, environment);
    }
    else{
      return junctionDecider.makeDecision(car, environment, roadStructureReader);
    }
  }

  public CarEnvironment getPrecedingCarOrCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader) {
    LaneReadable currentLane = roadStructureReader.getLaneReadable(currentCar.getLaneId());
    JunctionId nextJunctionId = currentLane.getOutgoingJunctionId();
    Optional<CarReadable> precedingCar = currentLane.getCarInFrontReadable(currentCar);
    Optional<JunctionId> nextCrossroadId;
    Optional<LaneId> incomingLaneId = Optional.of(currentLane.getLaneId());
    double distance;
    if (nextJunctionId.isCrossroad() || precedingCar.isPresent()) {
      distance = precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength())
          - currentCar.getPositionOnLane();
    } else {
      distance = 0;
      int offset = 0;
      LaneId nextLaneId;
      LaneReadable nextLane;
      while (precedingCar.isEmpty() && !nextJunctionId.isCrossroad()) {
        nextLaneId = currentCar.getRouteOffsetLaneId(offset++);
        if (nextLaneId == null) {
          break;
        }
        distance += currentLane.getLength(); // adds previous lane length
        nextLane = roadStructureReader.getLaneReadable(nextLaneId);
        nextJunctionId = nextLane.getOutgoingJunctionId();
        precedingCar = nextLane.getCarAtEntryReadable();
        incomingLaneId = Optional.of(nextLaneId);
        currentLane = nextLane;
      }
      distance += precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength())
          - currentCar.getPositionOnLane();
    }
    if (nextJunctionId.isCrossroad()) {
      nextCrossroadId = Optional.of(nextJunctionId);
    } else {
      nextCrossroadId = Optional.empty();
      incomingLaneId = Optional.empty();
    }
    return new CarEnvironment(distance, precedingCar, nextCrossroadId, incomingLaneId);
  }
}
