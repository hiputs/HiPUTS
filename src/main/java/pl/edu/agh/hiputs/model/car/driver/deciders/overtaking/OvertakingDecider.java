package pl.edu.agh.hiputs.model.car.driver.deciders.overtaking;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.HorizontalSign;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

@Slf4j
@RequiredArgsConstructor
public class OvertakingDecider {

  /**
   * Maximal time distance at which car should consider overtaking
   */
  private final double overtakingTimeDistance = 3.0;

  /**
   * Minimal difference in speed for overtaking (4.17 m/s ~> 15 km/h)
   */
  private final double minDeltaSpeed = 4.17;

  /**
   * Maximal acceleration for overtaken car, for now car cannot overtake an accelerating car
   */
  private final double accelerationThreshold = 0.2;

  /**
   * Time for safety distance (like after overtaking)
   */
  private final double safetyTimeDistance = 2.0;

  /**
   * Method decider, returns decision whether car should overtake or not
   */
  public boolean overtakeDecision(CarEditable car, CarEnvironment carEnvironment,
      RoadStructureReader roadStructureReader) {
    if (isCarCloseEnough(car.getSpeed(), carEnvironment) && isPrecedingCarSlower(car, carEnvironment)) {
      Optional<OvertakingEnvironment> overtakingEnvironment =
          getOvertakingInformation(car, carEnvironment, roadStructureReader);
      if (overtakingEnvironment.isPresent() && carEnvironment.getPrecedingCar().isPresent()) {
        CarReadable precedingCar = carEnvironment.getPrecedingCar().get();
        double safeDistanceForOvertakenCar = precedingCar.getSpeed() * safetyTimeDistance;
        double maximalDistanceForOvertaking =
            carEnvironment.getDistance() + precedingCar.getLength() + safeDistanceForOvertakenCar;
        double speedDelta;
        if (car.getSpeed() <= (precedingCar.getSpeed() + precedingCar.getAcceleration()) - minDeltaSpeed) {
          // todo for future investigate situation where car is moving slow but can accelerate
          // for now let's assume that it's effective speed is mean from actual and max speed
          speedDelta = estimatePotentialCarSpeed(car) - precedingCar.getSpeed();
        } else {
          speedDelta = car.getSpeed() - precedingCar.getSpeed();
        }
        double timeNeededForOvertaking = maximalDistanceForOvertaking / speedDelta;
        if (enoughSpaceForReturningBeforeOvertakenCar(safeDistanceForOvertakenCar, car, overtakingEnvironment.get(),
            precedingCar, timeNeededForOvertaking)) {
          if (overtakingEnvironment.get().getOppositeCar().isPresent()) {
            // check how oppositeCar position will change
            CarReadable carOnOppositeLane = overtakingEnvironment.get().getOppositeCar().get();
            double distanceCoveredByOppositeCar;
            if (carOnOppositeLane.getAcceleration() > accelerationThreshold) {
              // if car on opposite lane is accelerating let assume that it is accelerating to our max speed:
              distanceCoveredByOppositeCar = timeNeededForOvertaking * car.getMaxSpeed();
            } else {
              distanceCoveredByOppositeCar =
                  timeNeededForOvertaking * (carOnOppositeLane.getSpeed() + carOnOppositeLane.getAcceleration());
            }
            return maximalDistanceForOvertaking + distanceCoveredByOppositeCar < overtakingEnvironment.get()
                .getDistanceOnOppositeLane();
          } else {
            return maximalDistanceForOvertaking < overtakingEnvironment.get().getDistanceOnOppositeLane();
          }
        }
      }
    }
    return false;
  }

  /**
   * Check if preceding car is close enough to consider overtaking
   */
  private boolean isCarCloseEnough(double speed, CarEnvironment carEnvironment) {
    return carEnvironment.getDistance() <= speed * overtakingTimeDistance;
  }

  /**
   * Decide whether preceding car is moving slower and this car should try to overtake
   */
  public boolean isPrecedingCarSlower(CarEditable car, CarEnvironment carEnvironment) {
    if (carEnvironment.getPrecedingCar().isPresent()) {
      CarReadable precedingCar = carEnvironment.getPrecedingCar().get();
      double precedingCarSpeed = precedingCar.getSpeed();
      double precedingCarAcceleration = precedingCar.getAcceleration();
      if (precedingCarAcceleration <= accelerationThreshold
          && car.getSpeed() - (precedingCarSpeed + precedingCarAcceleration) >= minDeltaSpeed) {
        return true;
      }
      return precedingCarAcceleration <= accelerationThreshold
          && estimatePotentialCarSpeed(car) - (precedingCarSpeed + precedingCarAcceleration) >= minDeltaSpeed;
    }
    return false;
  }

  /**
   * Get potential car speed, for cars that are moving slow, slower than precedingCar + minDeltaSpeed
   */
  private double estimatePotentialCarSpeed(CarReadable car) {
    return (car.getMaxSpeed() + car.getSpeed()) / 2;
  }

  /**
   * Check if space before precedingCar is long enough,
   * takes into consideration changing in position of carBeforeOvertakenCar
   */
  private boolean enoughSpaceForReturningBeforeOvertakenCar(double safeDistanceForOvertakenCar, CarReadable car,
      OvertakingEnvironment overtakingEnvironment, CarReadable precedingCar, double timeNeededForOvertaking) {
    if (overtakingEnvironment.getCarBeforeOvertakenCar().isEmpty()) {
      return safeDistanceForOvertakenCar + car.getLength() + car.getMaxSpeed() * safetyTimeDistance
          < overtakingEnvironment.getDistanceBeforeOvertakenCar();
    } else {
      CarReadable carBeforeOvertakenCar = overtakingEnvironment.getCarBeforeOvertakenCar().get();
      double carBeforeOvertakenCarDelta = carBeforeOvertakenCar.getSpeed() - precedingCar.getSpeed();
      double carBeforeOvertakenCarPositionChange;
      if (carBeforeOvertakenCar.getAcceleration() < -accelerationThreshold) {
        // carBeforeOvertakenCar is slowing down todo for future inspect this case
        // for now let's assume that it acceleration will not change
        carBeforeOvertakenCarPositionChange = carBeforeOvertakenCarDelta * timeNeededForOvertaking + (
            carBeforeOvertakenCar.getAcceleration() * Math.pow(timeNeededForOvertaking, 2) / 2);
      } else {
        carBeforeOvertakenCarPositionChange = carBeforeOvertakenCarDelta * timeNeededForOvertaking;
      }
      return safeDistanceForOvertakenCar + car.getLength() + car.getMaxSpeed() * safetyTimeDistance
          < overtakingEnvironment.getDistanceBeforeOvertakenCar() + carBeforeOvertakenCarPositionChange;
    }
  }

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
    if (!canOvertakeOnLane(currentLane) || carEnvironment.getPrecedingCar().isEmpty()) {
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
          currentLane.getLength() - oppositeCar.map(CarReadable::getPositionOnLane).orElse(0.0)
              - currentCar.getPositionOnLane();
      overtakingEnvironment = new OvertakingEnvironment(oppositeCar, carBeforeOvertakenCar, distanceOnOppositeLane,
          distanceBeforeOvertakenCar);
    } else {
      overtakingEnvironment =
          searchRouteForOvertakingInformation(currentCar, overtakenCar, carBeforeOvertakenCar, oppositeCar,
              nextJunctionId, currentLane, oppositeLane, roadStructureReader);
    }
    return Optional.of(overtakingEnvironment);
  }

  /**
   * Iterate over Car route up to the closest crossroad or until all information are found
   *
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
        log.debug("Car: {} end of generated route;", currentCar.getCarId());
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
    distanceOnOppositeLane += oppositeLane.getLength() - oppositeCar.map(CarReadable::getPositionOnLane).orElse(0.0)
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
