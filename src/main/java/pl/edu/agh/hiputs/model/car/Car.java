package pl.edu.agh.hiputs.model.car;

import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Configurable;
import pl.edu.agh.hiputs.model.follow.IDecider;
import pl.edu.agh.hiputs.model.follow.IdmDecider;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

@Configurable
@Getter
@Builder
@AllArgsConstructor
public class Car implements CarEditable {

  /**
   * Unique car identifier.
   */
  @Builder.Default
  private final CarId carId = CarId.random();

  /**
   * Length of the car.
   */
  @Builder.Default
  private final double length = 5;

  /**
   * Maximum possible speed of the car.
   */
  @Builder.Default
  private final double maxSpeed = 20;

  /**
   * Decider instance
   */
  @Builder.Default
  private final IDecider decider = new IdmDecider();

  /**
   * Lane on which car is currently situated.
   */
  private LaneId laneId;

  /**
   * Position of car at its lane.
   */
  private double positionOnLane;

  /**
   * Route that car will follow and its location on this route.
   */
  private RouteWithLocation routeWithLocation;

  /**
   * Current speed of car.
   */
  @Builder.Default
  private double speed = 0;

  /**
   * Current acceleration of car.
   */
  @Builder.Default
  private double acceleration = 0;

  /**
   * Decision on how car state should be changed. Calculated by decider.
   */
  private Decision decision;

  @Override
  public void decide(RoadStructureReader roadStructureReader) {
    // make local decision based on read only road structure (watch environment) and save it locally

    //First prepare CarEnvironment
    CarEnvironment environment = this.getPrecedingCar(roadStructureReader);

    double acceleration = this.decider.makeDecision(this, environment);

    acceleration = limitAccelerationPreventReversing(acceleration, 1);

    LaneId currentLaneId = this.laneId;
    LaneReadable destinationCandidate = roadStructureReader.getLaneReadable(currentLaneId);
    int offset = 0;
    double desiredPosition = calculateFuturePosition(acceleration);
    Optional<LaneId> desiredLaneId;

    while (desiredPosition > destinationCandidate.getLength()) {
      desiredPosition -= destinationCandidate.getLength();
      desiredLaneId = routeWithLocation.getOffsetLaneId(offset + 1);
      if (desiredLaneId.isEmpty()) {
        currentLaneId = null;
        break;
      }
      offset++;
      currentLaneId = desiredLaneId.get();
      destinationCandidate = roadStructureReader.getLaneReadable(currentLaneId);
    }

    decision = Decision.builder()
        .acceleration(acceleration)
        .speed(this.speed + acceleration)
        .laneId(currentLaneId)
        .positionOnLane(desiredPosition)
        .offsetToMoveOnRoute(offset)
        .build();
  }

  private double limitAccelerationPreventReversing(double acceleration, int timeStep) {
    double minimalAcceleration = - (speed * timeStep);
    return Math.max(acceleration, minimalAcceleration);
  }

  @Override
  public Optional<CarUpdateResult> update() {
    if(!this.routeWithLocation.moveForward(decision.getOffsetToMoveOnRoute()) || decision.getLaneId() == null) // remove car from lane
        return Optional.empty();
    this.speed = decision.getSpeed();
    this.acceleration = decision.getAcceleration();
    CarUpdateResult carUpdateResult =
        new CarUpdateResult(this.laneId, decision.getLaneId(), decision.getPositionOnLane());
    this.laneId = decision.getLaneId();
    this.positionOnLane = decision.getPositionOnLane();
    return Optional.of(carUpdateResult);
  }

  /**
   * Search for preceding car or crossroad
   * on the way counting distance to car or to crossroad
   *
   * @return CarEnvironment
   */
  public CarEnvironment getPrecedingCar(RoadStructureReader roadStructureReader) {
    LaneReadable currentLane = roadStructureReader.getLaneReadable(this.laneId);
    JunctionId nextJunctionId = currentLane.getOutgoingJunctionId();
    Optional<CarReadable> precedingCar = currentLane.getCarInFrontReadable(this);
    Optional<JunctionId> nextCrossroadId;
    double distance;
    if (nextJunctionId.isCrossroad() || precedingCar.isPresent()) {
      distance = precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength())
          - this.positionOnLane;
    } else {
      distance = 0;
      int offset = 0;
      Optional<LaneId> nextLaneId;
      LaneReadable nextLane;
      while (precedingCar.isEmpty() && !nextJunctionId.isCrossroad()) {
        nextLaneId = routeWithLocation.getOffsetLaneId(offset++);
        if (nextLaneId.isEmpty())
          break;
        distance += currentLane.getLength(); // adds previous lane length
        nextLane = roadStructureReader.getLaneReadable(nextLaneId.get());
        nextJunctionId = nextLane.getOutgoingJunctionId();
        precedingCar = nextLane.getCarAtEntryReadable();
        currentLane = nextLane;
      }
      distance += precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength())
          - this.positionOnLane;
    }
    if (nextJunctionId.isCrossroad()) {
      nextCrossroadId = Optional.of(nextJunctionId);
    } else {
      nextCrossroadId = Optional.empty();
    }
    return new CarEnvironment(precedingCar, nextCrossroadId, distance);
  }

  public double calculateFuturePosition(double acceleration) {
    return this.positionOnLane + this.speed + acceleration / 2;
  }

  @Override
  public int compareTo(CarEditable anotherCar) {
    return this.carId.getValue().compareTo(anotherCar.getCarId().getValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Car car = (Car) o;
    return Objects.equals(carId, car.carId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(carId);
  }

  // TODO: create Driver class with an algorithm that will take into consideration all deciders
  //       and provide proper interface of a Car for the driver:
  //       accelerate, decelerate, change lane to right, change lane to left
  //    public void doMagic(RoadStructureReader roadStructureReader) {
  //        DecisionOfFollowingModel decisionOfFollowingModel = this.followingModel.doYourStuff(roadStructureReader);
  //        DecisionOfOtherModel decisionOfOtherModel = this.otherModel.doYourStuff(roadStructureReader);
  //        ...
  //        ...
  //        ...
  //        ...
  //
  //        magically combine all decisions
  //    }

}
