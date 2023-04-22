package pl.edu.agh.hiputs.model.car.driver.deciders.overtaking;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.HorizontalSign;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

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
            CarReadable carOnOppositeRoad = overtakingEnvironment.get().getOppositeCar().get();
            double distanceCoveredByOppositeCar;
            if (carOnOppositeRoad.getAcceleration() > accelerationThreshold) {
              // if car on opposite lane is accelerating let assume that it is accelerating to our max speed:
              distanceCoveredByOppositeCar = timeNeededForOvertaking * car.getMaxSpeed();
            } else {
              distanceCoveredByOppositeCar =
                  timeNeededForOvertaking * (carOnOppositeRoad.getSpeed() + carOnOppositeRoad.getAcceleration());
            }
            return maximalDistanceForOvertaking + distanceCoveredByOppositeCar < overtakingEnvironment.get()
                .getDistanceOnOppositeRoad();
          } else {
            return maximalDistanceForOvertaking < overtakingEnvironment.get().getDistanceOnOppositeRoad();
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
      if (precedingCarAcceleration <= accelerationThreshold
          && estimatePotentialCarSpeed(car) - (precedingCarSpeed + precedingCarAcceleration) >= minDeltaSpeed) {
        return true;
      }
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
    RoadReadable currentRoad = roadStructureReader.getRoadReadable(currentCar.getRoadId());
    if (!canOvertakeOnRoad(currentRoad) || carEnvironment.getPrecedingCar().isEmpty()) {
      return Optional.empty(); // we can't overtake
    }
    JunctionId nextJunctionId = currentRoad.getOutgoingJunctionId();
    RoadReadable oppositeRoad = roadStructureReader.getRoadReadable(currentRoad.getLeftNeighbor().get().getRoadId());
    CarReadable overtakenCar = carEnvironment.getPrecedingCar().get();
    Optional<CarReadable> carBeforeOvertakenCar = currentRoad.getCarInFrontReadable(overtakenCar); // find C car
    Optional<CarReadable> oppositeCar =
        oppositeRoad.getCarBeforePosition(oppositeRoad.getLength() - currentCar.getPositionOnRoad()); // find D car
    OvertakingEnvironment overtakingEnvironment;
    if (foundAllInformation(nextJunctionId, carBeforeOvertakenCar,
        oppositeCar)) { // for future if we find oppositeCar car, we need to find carBeforeOvertakenCar?
      double distanceBeforeOvertakenCar =
          carBeforeOvertakenCar.map(car -> car.getPositionOnRoad() - car.getLength()).orElse(currentRoad.getLength())
              - overtakenCar.getPositionOnRoad();
      double distanceOnOppositeRoad =
          currentRoad.getLength() - oppositeCar.map(CarReadable::getPositionOnRoad).orElse(0.0)
              - currentCar.getPositionOnRoad();
      overtakingEnvironment = new OvertakingEnvironment(oppositeCar, carBeforeOvertakenCar, distanceOnOppositeRoad,
          distanceBeforeOvertakenCar);
    } else {
      overtakingEnvironment =
          searchRouteForOvertakingInformation(currentCar, overtakenCar, carBeforeOvertakenCar, oppositeCar,
              nextJunctionId, currentRoad, oppositeRoad, roadStructureReader);
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
      RoadReadable currentRoad, RoadReadable oppositeRoad, RoadStructureReader roadStructureReader) {
    double distanceBeforeOvertakenCar = 0;
    double distanceOnOppositeRoad = 0;
    int offset = 0;
    Optional<RoadId> nextRoadId;
    RoadReadable nextRoad;
    RoadReadable nextOppositeRoad;
    while (!foundAllInformation(nextJunctionId, carBeforeOvertakenCar, oppositeCar)) {
      nextRoadId = currentCar.getRouteWithLocation().getOffsetRoadId(++offset);
      if (nextRoadId.isEmpty()) {
        break;
      }
      nextRoad = roadStructureReader.getRoadReadable(nextRoadId.get());
      if (!canOvertakeOnRoad(nextRoad)) {
        break; // we can't overtake any further
      }
      nextOppositeRoad = roadStructureReader.getRoadReadable(nextRoad.getLeftNeighbor().get().getRoadId());
      distanceBeforeOvertakenCar += currentRoad.getLength();
      distanceOnOppositeRoad += oppositeRoad.getLength();
      nextJunctionId = nextRoad.getOutgoingJunctionId();
      carBeforeOvertakenCar = nextRoad.getCarAtEntryReadable();
      oppositeCar = nextOppositeRoad.getCarAtExitReadable();
      currentRoad = nextRoad;
      oppositeRoad = nextOppositeRoad;
    }
    distanceBeforeOvertakenCar +=
        carBeforeOvertakenCar.map(car -> car.getPositionOnRoad() - car.getLength()).orElse(currentRoad.getLength())
            - overtakenCar.getPositionOnRoad();
    distanceOnOppositeRoad += oppositeRoad.getLength() - oppositeCar.map(CarReadable::getPositionOnRoad).orElse(0.0)
        - currentCar.getPositionOnRoad();
    return new OvertakingEnvironment(oppositeCar, carBeforeOvertakenCar, distanceOnOppositeRoad,
        distanceBeforeOvertakenCar);
  }

  private boolean canOvertakeOnRoad(RoadReadable road) {
    return road.getLeftNeighbor().isPresent() && road.getLeftNeighbor()
        .get()
        .getHorizontalSign()
        .equals(HorizontalSign.OPPOSITE_DIRECTION_DOTTED_LINE);
  }

  private boolean foundAllInformation(JunctionId nextJunctionId, Optional<CarReadable> carBeforeOvertakenCar,
      Optional<CarReadable> oppositeCar) {
    return nextJunctionId.isCrossroad() || (carBeforeOvertakenCar.isPresent() && oppositeCar.isPresent());
  }

}
