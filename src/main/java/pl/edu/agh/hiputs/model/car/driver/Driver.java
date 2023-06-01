package pl.edu.agh.hiputs.model.car.driver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
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
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

/**
 * Driver class with an algorithm that will take into consideration all deciders
 * and provide proper interface of a Car for the driver:
 * accelerate, decelerate, change lane to right, change lane to left
 */
@Slf4j
@RequiredArgsConstructor
public class Driver implements IDriver {

  private final CarProspector prospector;
  private final FunctionalDecider idmDecider;
  private final JunctionDecider junctionDecider;
  private final double distanceHeadway;
  private final double timeStep;
  private final double maxDeceleration;

  public Driver(DriverParameters parameters){
    this.prospector = new CarProspectorImpl(parameters.getViewRange());
    IFollowingModel idm = new Idm(parameters);
    this.idmDecider = new IdmDecider(idm);
    this.junctionDecider = new TrailJunctionDecider(prospector, idm, parameters);
    this.timeStep = parameters.getTimeStep();
    this.distanceHeadway = getDistanceHeadway();
    this.maxDeceleration = parameters.getIdmNormalDeceleration();
  }

  public Decision makeDecision(CarReadable car, RoadStructureReader roadStructureReader) {
    // make local decision based on read only road structure (watch environment) and save it locally


    log.debug("Car: " + car.getCarId() + ", road: " + car.getRoadId() + ", lane: " + car.getLaneId() +", position: " + car.getPositionOnLane()
              + ", acc: " + car.getAcceleration() + ", speed: " + car.getSpeed()
              + ", route0: " + car.getRouteOffsetRoadId(0) + ", route1: " + car.getRouteOffsetRoadId(1));


    //First prepare CarEnvironment

    double acceleration;
    CarEnvironment environment = prospector.getPrecedingCarOrCrossroad(car, roadStructureReader);

    log.trace("Car: " + car.getCarId() + ", environment: " + environment);

    Optional<CrossroadDecisionProperties> crossroadDecisionProperties = Optional.empty();
    try {
      if (environment.getPrecedingCar().isPresent()) {
        acceleration = idmDecider.makeDecision(car, environment, roadStructureReader);
      } else {
        JunctionDecision junctionDecision = junctionDecider.makeDecision(car, environment, roadStructureReader);
        acceleration = junctionDecision.getAcceleration();
        crossroadDecisionProperties = junctionDecision.getDecisionProperties();
      }
    }
    catch (Exception e){
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      log.error("Car: " + car.getCarId() + " caught error: \n" + e.getClass() + " : " + e.getMessage() + "\n" + sw.toString());
      acceleration = 0.1; //set default acceleration to move car to prevent stand here for a long time with error.
      crossroadDecisionProperties = Optional.empty();
    }

    /*if(limitAccelerationPreventReversing(acceleration, car, timeStep) <= acceleration) {
      CarEnvironment nextCrossroadEnvironment;
      if (environment.getPrecedingCar().isEmpty() && environment.getNextCrossroadId().isPresent()) {
        nextCrossroadEnvironment = prospector.getPrecedingCrossroad(car, roadStructureReader, environment.getNextCrossroadId().get());
      }
      else{
        nextCrossroadEnvironment = prospector.getPrecedingCrossroad(car, roadStructureReader);
      }
      if(nextCrossroadEnvironment.getNextCrossroadId().isPresent()) {
        CarReadable stoppedCar = createVirtualStoppedCarForIdmDecider();
        nextCrossroadEnvironment = new CarEnvironment(Optional.of(stoppedCar), nextCrossroadEnvironment.getNextCrossroadId(),
            nextCrossroadEnvironment.getDistance() + distanceHeadway);
        double nextJunctionAcceleration = idmDecider.makeDecision(car, nextCrossroadEnvironment, roadStructureReader);
        if(nextJunctionAcceleration < acceleration){
          acceleration = nextJunctionAcceleration;
          log.info("Car: " + car.getCarId() + " has reduced acceleration cause crossroad: " + nextCrossroadEnvironment.getNextCrossroadId().get());
        }
        /*JunctionDecision nextJunctionDecision = junctionDecider.makeDecision(car, nextCrossroadEnvironment, roadStructureReader);
        if(nextJunctionDecision.getAcceleration() < acceleration){
          acceleration = nextJunctionDecision.getAcceleration();
          log.info("Car: " + car.getCarId() + " has reduced acceleration cause crossroad: " + nextCrossroadEnvironment.getNextCrossroadId().get());
        }*/
      //}*/
    //}

    acceleration = limitAccelerationPreventReversing(acceleration, car, timeStep);

    RoadId currentRoadId = car.getRoadId();
    RoadReadable destinationCandidate = roadStructureReader.getRoadReadable(currentRoadId);
    LaneId currentLaneId = car.getLaneId();
    LaneReadable currentLane;
    int offset = 0;
    double desiredPosition = calculateFuturePosition(car, acceleration);
    Optional<RoadId> desiredRoadId;

    while (desiredPosition > destinationCandidate.getLength()) {
      desiredPosition -= destinationCandidate.getLength();
      desiredRoadId = car.getRouteOffsetRoadId(offset + 1);
      if (desiredRoadId.isEmpty()) {
        currentRoadId = null;
        break;
      }
      offset++;
      currentRoadId = desiredRoadId.get();
      destinationCandidate = roadStructureReader.getRoadReadable(currentRoadId);
      if(destinationCandidate == null){
        log.warn("Car: " + car.getCarId() + " Destination out of this node, positionRest: " + desiredPosition
            + ", desiredRoadId: " + currentRoadId);
        currentRoadId = null;
        currentLaneId = null;
        break;
      }

      // TODO: To be changed in future
      final RoadId finalCurrentRoadId = currentRoadId;
      currentLane = roadStructureReader
          .getLaneSuccessorsReadable(currentLaneId)
          .stream()
          .filter(l -> l.getRoadId()== finalCurrentRoadId)
          .findAny()
          .orElse(null);

      if (currentLane == null) {
        currentLaneId = destinationCandidate.getLanes().get(ThreadLocalRandom.current().nextInt(0, destinationCandidate.getLanes().size()));
        currentLane = roadStructureReader.getLaneReadable(currentLaneId);
      } else {
        currentLaneId = currentLane.getLaneId();
      }
    }

    double speed = car.getSpeed() + acceleration * timeStep;

    if(offset > 0 && crossroadDecisionProperties.isPresent()){
      if(crossroadDecisionProperties.get().getMovePermanentRoadId().isPresent()){
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
        .roadId(currentRoadId)
        .laneId(currentLaneId)
        .positionOnRoad(desiredPosition)
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

  private double calculateFuturePosition(CarReadable car, double acceleration) {
    return car.getPositionOnLane() + car.getSpeed() * timeStep + acceleration * timeStep * timeStep / 2;
  }
}
