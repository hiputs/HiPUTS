package pl.edu.agh.hiputs.model.follow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.car.CarBasicDeciderData;
import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneDirection;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneOnJunction;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneSubordination;

public class BasicJunctionDecider implements IDecider{

  private final IFollowingModel followingModel;

  public BasicJunctionDecider() {
    this.followingModel = new Idm();
  }

  public BasicJunctionDecider(Idm followingModel) {
    this.followingModel = followingModel;
  }

  private double lineHeight = 6.0;
  private double securityDelay = 1;
  private double crossroadMaxSpeed = 7;

  @Override
  public double makeDecision(CarReadable managedCar, CarEnvironment environment) {
    /*JunctionId junctionId = environment.getNextCrossroadId().get();
    if(junctionId==null){
    }
*/
    return 0;
  }

  public double makeDecision(CarReadable car, CarEnvironment environment, RoadStructureReader roadStructureReader) {
    double minArriveTime = getClosestConflictVehicleArriveTime(car, environment, roadStructureReader);
    double crossroadOutTime = calculateCrossroadOutTime(new CarBasicDeciderData(car.getSpeed(), environment.getDistance(), car.getLength()));

    double maxSpeed = environment.getDistance() < lineHeight * 4 ? crossroadMaxSpeed : car.getMaxSpeed();

    if(crossroadOutTime +  securityDelay > minArriveTime){
      final double acceleration = followingModel.calculateAcceleration(car.getSpeed(), maxSpeed,
          environment.getDistance() - lineHeight, -car.getSpeed());
      return acceleration;
    }

    final double acceleration =
        followingModel.calculateAcceleration(car.getSpeed(), maxSpeed, Double.MAX_VALUE, -car.getSpeed());
    return acceleration;
  }

  private double getClosestConflictVehicleArriveTime(CarReadable car, CarEnvironment environment, RoadStructureReader roadStructureReader){
    JunctionId nextJunctionId = environment.getNextCrossroadId().get();
    JunctionReadable junction = roadStructureReader.getJunctionReadable(nextJunctionId);
    List<LaneOnJunction> lanesOnJunction = junction.streamLanesOnJunction().toList();
    LaneId incomingLaneId = environment.getIncomingLaneId().get();
    LaneId outgoingLaneId = getNextOutgoingLane(car, nextJunctionId, roadStructureReader);

    List<LaneId> conflictLanesId = getConflictLanesId(lanesOnJunction, incomingLaneId, outgoingLaneId);

    List<CarBasicDeciderData> conflictCars = getConflictCars(conflictLanesId, roadStructureReader);
    return getFirstArriveCarTime(conflictCars);
  }

  private double getFirstArriveCarTime(List<CarBasicDeciderData> conflictCars){
    double minArriveTime = Double.POSITIVE_INFINITY;
    for (CarBasicDeciderData car: conflictCars) {
      double arriveTime = calculateArriveTime(car);
      if(arriveTime < minArriveTime){
        minArriveTime = arriveTime;
      }
    }
    return minArriveTime;
  }

  private double calculateArriveTime(CarBasicDeciderData carData){
    return carData.getDistance() / carData.getSpeed();
  }

  private double calculateCrossroadOutTime(CarBasicDeciderData carData){
    return (carData.getDistance() + carData.getLength() + lineHeight) / carData.getSpeed();
  }

  private List<CarBasicDeciderData> getConflictCars(List<LaneId> conflictLanes, RoadStructureReader roadStructureReader){
    List<CarBasicDeciderData> conflictCars = new ArrayList<>();
    for (LaneId laneId: conflictLanes) {
      LaneReadable lane = laneId.getReadable(roadStructureReader);
      CarReadable conflictCar = lane.streamCarsFromExitReadable().findFirst().get();
      if(conflictCar != null){
        double distance = lane.getLength() - conflictCar.getPositionOnLane();
        conflictCars.add(new CarBasicDeciderData(conflictCar.getSpeed(), distance, conflictCar.getLength()));
      }
    }


    return conflictCars;
  }

  private List<LaneId> getConflictLanesId(List<LaneOnJunction> lanesOnJunction, LaneId incomingLaneId, LaneId outgoingLaneId){
    LaneOnJunction incomingLaneOnJunction = lanesOnJunction.stream().filter(
        laneOnJunction -> laneOnJunction.getLaneId() == incomingLaneId
            && laneOnJunction.getDirection().equals(LaneDirection.INCOMING)).findFirst().get();
    LaneOnJunction outgoingLaneOnJunction = lanesOnJunction.stream().filter(
        laneOnJunction -> laneOnJunction.getLaneId() == outgoingLaneId
            && laneOnJunction.getDirection().equals(LaneDirection.OUTGOING)).findFirst().or(()->Optional.of(incomingLaneOnJunction)).get();

    Stream<LaneOnJunction> conflictLanes;
    conflictLanes = lanesOnJunction.stream()
        .filter(laneOnJunction -> laneOnJunction.getDirection().equals(LaneDirection.INCOMING)
            && isLaneOnJunctionIndexInRange(laneOnJunction.getLaneIndexOnJunction(), incomingLaneOnJunction.getLaneIndexOnJunction(), outgoingLaneOnJunction.getLaneIndexOnJunction())).toList().stream();

    if(incomingLaneOnJunction.getSubordination().equals(LaneSubordination.NOT_SUBORDINATE)){
      conflictLanes = conflictLanes.filter(laneOnJunction -> laneOnJunction.getSubordination().equals(LaneSubordination.NOT_SUBORDINATE)).toList().stream();
    }
    else{
      conflictLanes = Stream.concat(conflictLanes, lanesOnJunction.stream()
          .filter(laneOnJunction -> laneOnJunction.getDirection().equals(LaneDirection.INCOMING)
          && laneOnJunction.getSubordination().equals(LaneSubordination.NOT_SUBORDINATE))).distinct().toList().stream();
    }
    return conflictLanes.map(laneOnJunction -> laneOnJunction.getLaneId()).toList();
  }

  private boolean isLaneOnJunctionIndexInRange(int index, int incoming, int outgoing){
    if(incoming <= outgoing){
      return index > incoming && index < outgoing;
    }
    else{
      return index > incoming || index < outgoing;
    }
  }

  private LaneId getNextOutgoingLane(CarReadable car, JunctionId junctionId, RoadStructureReader roadStructureReader){
    LaneId outgoingLaneId = null;
    int offset = 0;
    LaneId tmpLane;
    do{
      tmpLane = car.getRouteOffsetLaneId(offset++);
      if(tmpLane != null && tmpLane.getReadable(roadStructureReader).getIncomingJunctionId().equals(junctionId)){
        outgoingLaneId = tmpLane;
      }
    }
    while(outgoingLaneId == null && tmpLane != null);
    return outgoingLaneId;
  }
}
