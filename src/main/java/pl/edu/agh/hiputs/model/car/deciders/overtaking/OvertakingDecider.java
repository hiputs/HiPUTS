package pl.edu.agh.hiputs.model.car.deciders.overtaking;

import java.util.Optional;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.HorizontalSign;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

public class OvertakingDecider {

  /**
   * Searching for car (or crossroad) on opposite lane and distance to it
   * and for car (or crossroad) before overtaken car and distance between it and overtaken car.
   * It takes into account horizontal signs, if both cars are not found either distance is calculated to crossroad or
   * until car can cross to opposite lane.
   *
   * @param currentCar
   * @param carEnvironment
   * @param roadStructureReader
   *
   * @return Optional of OvertakingEnvironment with cars (if found) and distances either to cars, crossroad or place
   *     where car can't overtake
   *     or empty, when car can't overtake (left horizontal sign does not allow overtaking)
   */
  public Optional<OvertakingEnvironment> getOvertakingInformation(CarEditable currentCar, CarEnvironment carEnvironment,
      RoadStructureReader roadStructureReader) {
    LaneReadable currentLane = roadStructureReader.getLaneReadable(currentCar.getLaneId());
    if (!canOvertakeOnLane(currentLane)) {
      return Optional.empty(); // we can't overtake
    }
    JunctionId nextJunctionId = currentLane.getOutgoingJunctionId();
    LaneReadable oppositeLane = roadStructureReader.getLaneReadable(currentLane.getLeftNeighbor().get().getLaneId());
    CarReadable overtakenCar = carEnvironment.getPrecedingCar().get();
    Optional<CarReadable> carBeforeOvertakenCar = currentLane.getCarInFrontReadable(overtakenCar); // find C car
    Optional<CarReadable> oppositeCar =
        oppositeLane.getCarBeforePosition(oppositeLane.getLength() - currentCar.getPositionOnLane()); // find D car
    OvertakingEnvironment overtakingEnvironment;
    if (foundAllInformation(nextJunctionId, carBeforeOvertakenCar,
        oppositeCar)) { // for future if we find oppositeCar car, we need to find carBeforeOvertakenCar?
      double distanceBeforeOvertakenCar =
          carBeforeOvertakenCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength())
              - overtakenCar.getPositionOnLane();
      double distanceOnOppositeLane =
          currentLane.getLength() - oppositeCar.map(car -> car.getPositionOnLane()).orElse(0.0)
              - currentCar.getPositionOnLane();
      overtakingEnvironment = new OvertakingEnvironment(oppositeCar, carBeforeOvertakenCar, distanceOnOppositeLane,
          distanceBeforeOvertakenCar);
    } else {
      overtakingEnvironment =
          searchRouteForOvertakingInformation(currentCar, overtakenCar, carBeforeOvertakenCar, oppositeCar, nextJunctionId,
              currentLane, oppositeLane, roadStructureReader);
    }
    return Optional.of(overtakingEnvironment);
  }

  /**
   * Iterate over Car route up to the closest crossroad or until all information are found
   * @return
   */
  private OvertakingEnvironment searchRouteForOvertakingInformation(CarEditable currentCar, CarReadable overtakenCar,
      Optional<CarReadable> carBeforeOvertakenCar, Optional<CarReadable> oppositeCar, JunctionId nextJunctionId,
      LaneReadable currentLane, LaneReadable oppositeLane, RoadStructureReader roadStructureReader) {
    double distanceBeforeOvertakenCar = 0;
    double distanceOnOppositeLane = 0;
    int offset = 0;
    Optional<LaneId> nextLaneId;
    LaneReadable nextLane;
    LaneReadable nextOppositeLane;
    while (!foundAllInformation(nextJunctionId, carBeforeOvertakenCar, oppositeCar)) {
      nextLaneId = currentCar.getRouteWithLocation().getOffsetLaneId(++offset);
      if (nextLaneId.isEmpty()) {
        break;
      }
      nextLane = roadStructureReader.getLaneReadable(nextLaneId.get());
      if (!canOvertakeOnLane(nextLane)) {
        break; // we can't overtake any further
      }
      nextOppositeLane = roadStructureReader.getLaneReadable(nextLane.getLeftNeighbor().get().getLaneId());
      distanceBeforeOvertakenCar += currentLane.getLength();
      distanceOnOppositeLane += oppositeLane.getLength();
      nextJunctionId = nextLane.getOutgoingJunctionId();
      carBeforeOvertakenCar = nextLane.getCarAtEntryReadable();
      oppositeCar = nextOppositeLane.getCarAtExitReadable();
      currentLane = nextLane;
      oppositeLane = nextOppositeLane;
    }
    distanceBeforeOvertakenCar +=
        carBeforeOvertakenCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength())
            - overtakenCar.getPositionOnLane();
    distanceOnOppositeLane += oppositeLane.getLength() - oppositeCar.map(car -> car.getPositionOnLane()).orElse(0.0)
        - currentCar.getPositionOnLane();
    return new OvertakingEnvironment(oppositeCar, carBeforeOvertakenCar, distanceOnOppositeLane,
        distanceBeforeOvertakenCar);
  }

  private boolean canOvertakeOnLane(LaneReadable lane) {
    return lane.getLeftNeighbor().isPresent() && lane.getLeftNeighbor()
        .get()
        .getHorizontalSign()
        .equals(HorizontalSign.OPPOSITE_DIRECTION_DOTTED_LINE);
  }

  private boolean foundAllInformation(JunctionId nextJunctionId, Optional<CarReadable> carBeforeOvertakenCar,
      Optional<CarReadable> oppositeCar) {
    return nextJunctionId.isCrossroad() || (carBeforeOvertakenCar.isPresent() && oppositeCar.isPresent());
  }

}
