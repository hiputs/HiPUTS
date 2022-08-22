package pl.edu.agh.hiputs.model.car.driver.deciders.junction;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspector;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspectorImpl;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.FunctionalDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.Idm;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.IFollowingModel;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneOnJunction;

@Slf4j
public class BasicJunctionDecider implements FunctionalDecider {

  private final CarProspector prospector = new CarProspectorImpl();

  private final IFollowingModel followingModel = new Idm();

  public BasicJunctionDecider() {}

  private final double lineHeight = 6.0;
  private final double securityDelay = 2;
  private final double crossroadMaxSpeed = 7;
  private final double speedThreshold = 2;
  private final double freeAcceleration = 1;

  public double makeDecision(CarReadable car, CarEnvironment environment, RoadStructureReader roadStructureReader) {
    double minArriveTime = getClosestConflictVehicleArriveTime(car, environment, roadStructureReader);

    double crossroadOutTime = calculateCrossroadOutTime(new CarBasicDeciderData(car.getSpeed(), environment.getDistance(), car.getLength()));

    double maxSpeed = environment.getDistance() < lineHeight * 4 ? crossroadMaxSpeed : car.getMaxSpeed();

    if(crossroadOutTime +  securityDelay > minArriveTime){
      final double acceleration = followingModel.calculateAcceleration(car.getSpeed(), maxSpeed,
          environment.getDistance() - lineHeight, car.getSpeed());
      return acceleration;
    }

    final double acceleration =
        followingModel.calculateAcceleration(car.getSpeed(), maxSpeed, Double.MAX_VALUE, 0);
    return acceleration;
  }

  private double getClosestConflictVehicleArriveTime(CarReadable car, CarEnvironment environment, RoadStructureReader roadStructureReader){
    if(environment.getNextCrossroadId().isEmpty() || environment.getIncomingLaneId().isEmpty()){
      return Double.MAX_VALUE;
    }
    JunctionId nextJunctionId = environment.getNextCrossroadId().get();
    JunctionReadable junction = roadStructureReader.getJunctionReadable(nextJunctionId);
    List<LaneOnJunction> lanesOnJunction = junction.streamLanesOnJunction().toList();
    LaneId incomingLaneId = environment.getIncomingLaneId().get();
    LaneId outgoingLaneId = prospector.getNextOutgoingLane(car, nextJunctionId, roadStructureReader);

    List<LaneId> conflictLanesId = prospector.getConflictLaneIds(lanesOnJunction, incomingLaneId, outgoingLaneId);

    List<CarBasicDeciderData> conflictCars = prospector.getConflictCars(conflictLanesId, roadStructureReader);
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
    if(carData.getDistance() < lineHeight * 2 && carData.getSpeed() < speedThreshold){
      return Math.sqrt(2 * (carData.getDistance() + carData.getLength() + lineHeight) / freeAcceleration);
    }
    return (carData.getDistance() + carData.getLength() + lineHeight) / carData.getSpeed();
  }
}
