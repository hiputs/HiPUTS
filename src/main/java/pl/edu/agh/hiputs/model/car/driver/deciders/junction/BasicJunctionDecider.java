package pl.edu.agh.hiputs.model.car.driver.deciders.junction;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarPrecedingEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspector;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.FunctionalDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.ICarFollowingModel;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadOnJunction;

@Slf4j
public class BasicJunctionDecider implements FunctionalDecider {

  private final CarProspector prospector;

  private final ICarFollowingModel followingModel;

  public BasicJunctionDecider(CarProspector prospector, ICarFollowingModel followingModel) {
    this.prospector = prospector;
    this.followingModel = followingModel;
  }

  private final double lineHeight = 6.0;
  private final double securityDelay = 2;
  private final double crossroadMaxSpeed = 7;
  private final double speedThreshold = 2;
  private final double freeAcceleration = 1;

  public double makeDecision(CarReadable car, CarPrecedingEnvironment environment, RoadStructureReader roadStructureReader) {
    double maxSpeed = environment.getDistance() < lineHeight * 8 ? crossroadMaxSpeed : car.getMaxSpeed();
    log.warn("Car: {} crossroad workaround remove when repair errors", car.getCarId());
    return followingModel.calculateAcceleration(car.getSpeed(), maxSpeed, Double.MAX_VALUE, 0);

    /*double minArriveTime = getClosestConflictVehicleArriveTime(car, environment, roadStructureReader);
    CarEnvironment precedingCarInfo = prospector.getPrecedingCar(car, roadStructureReader);

    double crossroadOutTime = calculateCrossroadOutTime(new CarBasicDeciderData(car.getSpeed(), environment.getDistance(), car.getLength()));

    double maxSpeed = environment.getDistance() < lineHeight * 4 ? crossroadMaxSpeed : car.getMaxSpeed();

    if(crossroadOutTime +  securityDelay > minArriveTime){
      double acceleration = followingModel.calculateAcceleration(car.getSpeed(), maxSpeed,
          environment.getDistance() - lineHeight, car.getSpeed());
      return acceleration;
    }

    double acceleration = 0;
    if(precedingCarInfo.getPrecedingCar().isPresent()){
      CarReadable precedingCar = precedingCarInfo.getPrecedingCar().get();
      log.trace("Car: " + car.getCarId() + " found car after crossroad: car: " + precedingCar.getCarId() + " speed: " + precedingCar.getSpeed()
          + " position: " + precedingCar.getPositionOnLane() + " lane: " + precedingCar.getLaneId());
      acceleration = followingModel.calculateAcceleration(car.getSpeed(), maxSpeed, precedingCarInfo.getDistance(), car.getSpeed() - precedingCar.getSpeed());
    }
    else {
      log.trace("Car: " + car.getCarId() + " does not found car after crossroad");
      acceleration = followingModel.calculateAcceleration(car.getSpeed(), maxSpeed, Double.MAX_VALUE, 0);
    }
    return acceleration;*/
  }

  private double getClosestConflictVehicleArriveTime(CarReadable car, CarPrecedingEnvironment environment, RoadStructureReader roadStructureReader){
    if(environment.getNextCrossroadId().isEmpty() || environment.getIncomingRoadId().isEmpty()){
      return Double.MAX_VALUE;
    }
    JunctionId nextJunctionId = environment.getNextCrossroadId().get();
    JunctionReadable junction = roadStructureReader.getJunctionReadable(nextJunctionId);
    List<RoadOnJunction> roadsOnJunction = junction.streamRoadOnJunction().toList();
    RoadId incomingRoadId = environment.getIncomingRoadId().get();
    RoadId outgoingRoadId = prospector.getNextOutgoingRoad(car, nextJunctionId, roadStructureReader);

    List<RoadId> conflictRoadsId = prospector.getConflictRoadIds(roadsOnJunction, incomingRoadId, outgoingRoadId,
        car.getCarId(), roadStructureReader);

    List<CarBasicDeciderData> conflictCars = prospector.getFirstCarsFromRoads(conflictRoadsId, roadStructureReader);
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
