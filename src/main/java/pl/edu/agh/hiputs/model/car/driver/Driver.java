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
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CrossroadDecisionProperties;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.JunctionDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.JunctionDecision;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.TrailJunctionDecider;
import pl.edu.agh.hiputs.model.id.CarId;
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
  private final JunctionDecider junctionDecider;
  private final double distanceHeadway;
  private final double timeStep;
  private final double maxDeceleration;

  public Driver(CarReadable car, DriverParameters parameters){
    this.car = car;
    this.prospector = new CarProspectorImpl();
    IFollowingModel idm = new Idm(parameters);
    this.idmDecider = new IdmDecider(idm);
    this.junctionDecider = new TrailJunctionDecider(prospector, idm, new DriverParameters());
    this.timeStep = parameters.getDriverTimeStep();
    this.distanceHeadway = getDistanceHeadway();
    this.maxDeceleration = parameters.getIdmNormalDeceleration();
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

    Optional<CrossroadDecisionProperties> crossroadDecisionProperties = Optional.empty();
    if(environment.getPrecedingCar().isPresent()){
      acceleration = idmDecider.makeDecision(car, environment, roadStructureReader);
    }
    else{
      JunctionDecision junctionDecision = junctionDecider.makeDecision(car, environment, roadStructureReader);
      acceleration = junctionDecision.getAcceleration();
      crossroadDecisionProperties = junctionDecision.getDecisionProperties();
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

    double speed = car.getSpeed() + acceleration * timeStep;

    if(offset > 0 && crossroadDecisionProperties.isPresent()){
      if(crossroadDecisionProperties.get().getMovePermanentLaneId().isPresent()){
        CarEnvironment precedingCarInfo = prospector.getPrecedingCar(car, roadStructureReader);
        if(precedingCarInfo.getPrecedingCar().isPresent() && precedingCarInfo.getDistance() < (speed * speed / maxDeceleration / 2)){
          CarReadable precedingCar = precedingCarInfo.getPrecedingCar().get();
          speed = Math.min(speed, Math.max(precedingCar.getSpeed() - maxDeceleration, 0) * 0.8);
          desiredPosition = Math.min(desiredPosition,
              precedingCar.getPositionOnLane() - Math.min(0.1, precedingCar.getPositionOnLane() * 0.1));
          log.trace("Car: " + car.getCarId() + " finish move permanent and limit speed to car: " + precedingCar.getCarId() + ", speed: " + precedingCar.getSpeed() + ", position: " + precedingCar.getPositionOnLane());
        }
        else{
          log.trace("Car: " + car.getCarId() + " finish move permanent without preceding car");
        }
      }
    }

    final Decision decision = Decision.builder()
        .acceleration(acceleration)
        .speed(speed)
        .laneId(currentLaneId)
        .positionOnLane(desiredPosition)
        .offsetToMoveOnRoute(offset)
        .crossroadDecisionProperties(crossroadDecisionProperties)
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
