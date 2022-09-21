package pl.edu.agh.hiputs.model.car.driver;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspector;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspectorImpl;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.car.driver.deciders.FunctionalDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.IFollowingModel;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.Idm;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.IdmDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.TrailJunctionDecider;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

/**
 * Driver class with an algorithm that will take into consideration all deciders
 * and provide proper interface of a Car for the driver:
 * accelerate, decelerate, change lane to right, change lane to left
 */
@Slf4j
@RequiredArgsConstructor
public class Driver implements IDriver {

  private final CarReadable car;
  private final CarProspector prospector;
  private final FunctionalDecider idmDecider;
  private final FunctionalDecider junctionDecider;
  private final double distanceHeadway;
  private final double timeStep;

  public Driver(CarReadable car, DriverParameters parameters){
    this.car = car;
    this.prospector = new CarProspectorImpl();
    IFollowingModel idm = new Idm(parameters);
    this.idmDecider = new IdmDecider(idm);
    this.junctionDecider = new TrailJunctionDecider(prospector, idm, new DriverParameters());
    this.timeStep = parameters.getDriverTimeStep();
    this.distanceHeadway = getDistanceHeadway();
  }
  public Decision makeDecision(RoadStructureReader roadStructureReader) {
    // make local decision based on read only road structure (watch environment) and save it locally


    log.debug("Car: " + car.getCarId() + ", lane: " + car.getLaneId() + ", position: " + car.getPositionOnLane()
              + ", acc: " + car.getAcceleration() + ", speed: " + car.getSpeed()
              + ", route0: " + car.getRouteOffsetLaneId(0) + ", route1: " + car.getRouteOffsetLaneId(1));


    //First prepare CarEnvironment

    double acceleration;
    CarEnvironment environment = prospector.getPrecedingCarOrCrossroad(car, roadStructureReader);

    log.trace("Car: " + car.getCarId() + ", environment: " + environment);

    if(environment.getPrecedingCar().isPresent()){
      acceleration = idmDecider.makeDecision(car, environment, roadStructureReader);
    }
    else{
      acceleration = junctionDecider.makeDecision(car, environment, roadStructureReader);
    }
    //= this.decider.makeDecision(this, roadStructureReader);

    acceleration = limitAccelerationPreventReversing(acceleration, car, timeStep);

    LaneId currentLaneId = this.car.getLaneId();
    LaneReadable destinationCandidate = roadStructureReader.getLaneReadable(currentLaneId);
    int offset = 0;
    double desiredPosition = this.calculateFuturePosition(acceleration);
    Optional<LaneId> desiredLaneId;

    while (desiredPosition > destinationCandidate.getLength()) {
      desiredPosition -= destinationCandidate.getLength();
      desiredLaneId = car.getRouteOffsetLaneId(offset + 1);
      if (desiredLaneId.isEmpty()) {
        currentLaneId = null;
        break;
      }
      offset++;
      currentLaneId = desiredLaneId.get();
      destinationCandidate = roadStructureReader.getLaneReadable(currentLaneId);
      if(destinationCandidate == null){
        log.warn("Car: " + car.getCarId() + " Destination out of this node, positionRest: " + desiredPosition
            + ", desiredLaneId: " + currentLaneId);
        currentLaneId = null;
        break;
      }
    }

    final Decision decision = Decision.builder()
        .acceleration(acceleration)
        .speed(car.getSpeed() + acceleration * timeStep)
        .laneId(currentLaneId)
        .positionOnLane(desiredPosition)
        .offsetToMoveOnRoute(offset)
        .build();

    log.debug("Car: " + car.getCarId() + ", decision: " + decision);

    return decision;
  }

  @Override
  public double getDistanceHeadway() {
    return distanceHeadway;
  }

  /**
   We limit acceleration for prevent car move backward
   v = v0 + a * t  // we want v = 0 - car will be stopped
   0 = v0 + a * t
   - v0 = a * t
   - v0 / t = a
   a = - (v0 / t)
   minimalAcceleration = - (speed / timeStep)
   **/
  private double limitAccelerationPreventReversing(double acceleration, CarReadable car, double timeStep) {
    double minimalAcceleration = - (car.getSpeed() / timeStep);
    return Math.max(acceleration, minimalAcceleration);
  }

  private double calculateFuturePosition(double acceleration) {
    return car.getPositionOnLane() + car.getSpeed() * timeStep + acceleration * timeStep * timeStep / 2;
  }
}
