package pl.edu.agh.hiputs.model.car;

import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import pl.edu.agh.hiputs.model.car.driver.Driver;
import pl.edu.agh.hiputs.model.car.driver.IDriver;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

@Slf4j
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
   * Driver instance
   */
  private final IDriver driver = new Driver(this);

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
  @Setter
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
    decision = this.driver.makeDecision(roadStructureReader);
  }

  @Override
  public Optional<CarUpdateResult> update() {
    if(!this.routeWithLocation.moveForward(decision.getOffsetToMoveOnRoute()) || decision.getLaneId() == null) { // remove car from lane
      log.trace("Car: " + this.getCarId() + " was removed due to finish his route");
      return Optional.empty();
    }
    this.speed = decision.getSpeed();
    this.acceleration = decision.getAcceleration();
    CarUpdateResult carUpdateResult =
        new CarUpdateResult(this.laneId, decision.getLaneId(), decision.getPositionOnLane());
    this.laneId = decision.getLaneId();
    this.positionOnLane = decision.getPositionOnLane();

    Optional<LaneId> laneId = this.getRouteOffsetLaneId(0);
    if(laneId.isPresent() && !this.laneId.equals(laneId.get())){
      log.warn("Car: " + this.getCarId() + " was removed due to lane offset error");
      return Optional.empty();
    }
    return Optional.of(carUpdateResult);
  }

  @Override
  public void setPositionOnLaneAndSpeed(double position, double speed) {
    if(positionOnLane > position){
      positionOnLane = position;
    }
    if(this.speed > speed){
      this.speed = speed;
    }
  }

  @Override
  public Optional<LaneId> getRouteOffsetLaneId(int offset){
      return this.routeWithLocation.getOffsetLaneId(offset);
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

}
