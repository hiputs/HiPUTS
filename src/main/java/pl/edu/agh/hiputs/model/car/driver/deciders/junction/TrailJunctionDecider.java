package pl.edu.agh.hiputs.model.car.driver.deciders.junction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspector;
import pl.edu.agh.hiputs.model.car.driver.DriverParameters;
import pl.edu.agh.hiputs.model.car.driver.deciders.FunctionalDecider;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.IFollowingModel;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneOnJunction;

public class TrailJunctionDecider implements FunctionalDecider {

  private final CarProspector prospector;
  private final IFollowingModel followingModel;
  private final double timeDelta;
  private final double conflictAreaLength;
  private final int accelerationDelta;
  private final double maxAcceleration;
  private final double maxDeceleration;

  public TrailJunctionDecider(CarProspector prospector, IFollowingModel followingModel, DriverParameters parameters){
      this.prospector = prospector;
      this.followingModel = followingModel;
      this.timeDelta = parameters.getTrailTimeDelta();
      this.conflictAreaLength = parameters.getTrailConflictAreaLength();
      this.maxAcceleration = parameters.getIdmNormalAcceleration();
      this.maxDeceleration = parameters.getIdmNormalDeceleration();
      this.accelerationDelta = parameters.getIdmDelta();
  }

  @Override
  public double makeDecision(CarReadable managedCar, CarEnvironment environment,
      RoadStructureReader roadStructureReader) {

    //getAllConflictVehiclesProperties(managedCar, environment, roadStructureReader);
    List<ConflictVehicleProperties> conflictVehiclesProperties = getAllConflictVehiclesProperties(managedCar, environment, roadStructureReader)
        .stream().sorted(Comparator.comparingDouble(ConflictVehicleProperties::getTte_a)).toList();

    CarEnvironment precedingCarInfo = prospector.getPrecedingCar(managedCar, roadStructureReader);

    if(conflictVehiclesProperties.isEmpty()){
      return getCrossroadAccelerationResult(managedCar.getSpeed(), managedCar.getMaxSpeed(), precedingCarInfo);
    }

    CarTrailDeciderData managedCarTrailDataFreeAccel = new CarTrailDeciderData(managedCar.getSpeed(), environment.getDistance(), managedCar.getLength(), maxAcceleration, managedCar.getMaxSpeed(), Optional.empty());

    //calculate time to passable (cross and merge)

    //double currentTte = calculateTimeToEnter(managedCarTrailDataFreeAccel, TimeCalculationOption.FreeAcceleration);
    double currentTtc_cross = calculateTimeToClearCrossing(managedCarTrailDataFreeAccel, conflictAreaLength, TimeCalculationOption.FreeAcceleration);
    double currentTtc_merge = calculateTimeToClearMerge(managedCarTrailDataFreeAccel, TimeCalculationOption.FreeAcceleration);
    //double currentTtp = calculateTimeToPassableCrossing(managedCar, managedCarTrailDataFreeAccel, conflictAreaLength, TimeCalculationOption.FreeAcceleration);

    ConflictVehicleProperties firstConflictVehicle = conflictVehiclesProperties.get(0);

    double currentTtc = firstConflictVehicle.isCrossConflict() ? currentTtc_cross : currentTtc_merge;
    if(timeDelta * currentTtc < firstConflictVehicle.getTte_a()){
      //drive

      if(!firstConflictVehicle.isCrossConflict()){
        double freeAcceleration = maxAcceleration * (1 - Math.pow(managedCar.getSpeed() / managedCar.getMaxSpeed(),
            timeDelta));
        double tDeltaV = Math.max((firstConflictVehicle.getCar().getSpeed() - maxDeceleration * currentTtc - managedCar.getSpeed() - freeAcceleration * currentTtc) / maxDeceleration, 0);
        if(timeDelta * (tDeltaV + currentTtc) < firstConflictVehicle.getTte_b()){
          return getCrossroadAccelerationResult(managedCar.getSpeed(), managedCar.getMaxSpeed(), precedingCarInfo);
        }
      }
      else{
        return getCrossroadAccelerationResult(managedCar.getSpeed(), managedCar.getMaxSpeed(), precedingCarInfo);
      }
    }

    return getStopAccelerationResult(managedCar.getSpeed(), managedCar.getMaxSpeed(), environment.getDistance() - conflictAreaLength / 2);
  }

  private double getCrossroadAccelerationResult(double speed, double desiredSpeed, CarEnvironment precedingCarInfo) {
    if(precedingCarInfo.getPrecedingCar().isEmpty()) {
      return followingModel.calculateAcceleration(speed, desiredSpeed, Double.MAX_VALUE, 0);
    }
    else{
      return followingModel.calculateAcceleration(speed, desiredSpeed, precedingCarInfo.getDistance(), speed - precedingCarInfo.getPrecedingCar().get().getSpeed());
    }
  }

  /*private double getFreeAccelerationResult(double speed, double desiredSpeed) {
  }*/

  private double getStopAccelerationResult(double speed, double desiredSpeed, double distance) {
    return followingModel.calculateAcceleration(speed, desiredSpeed, distance, speed);
  }

  private List<ConflictVehicleProperties> getAllConflictVehiclesProperties(CarReadable currentCar, CarEnvironment environment, RoadStructureReader roadStructureReader){
    if(environment.getNextCrossroadId().isEmpty() || environment.getIncomingLaneId().isEmpty()){
      return new ArrayList<>();
    }

    JunctionId nextJunctionId = environment.getNextCrossroadId().get();
    JunctionReadable junction = roadStructureReader.getJunctionReadable(nextJunctionId);
    List<LaneOnJunction> lanesOnJunction = junction.streamLanesOnJunction().toList();
    LaneId incomingLaneId = environment.getIncomingLaneId().get();
    LaneId outgoingLaneId = prospector.getNextOutgoingLane(currentCar, nextJunctionId, roadStructureReader);

    List<LaneId> conflictLanesId = prospector.getConflictLaneIds(lanesOnJunction, incomingLaneId, outgoingLaneId);
    List<CarTrailDeciderData> conflictCars = prospector.getAllCarsFromLanes(conflictLanesId, roadStructureReader, conflictAreaLength);
    List<ConflictVehicleProperties> conflictVehiclesProperties = conflictCars.stream().map(
        conflictCar -> getConflictProperties(currentCar, conflictCar, conflictAreaLength, outgoingLaneId)).toList();

    return conflictVehiclesProperties;
  }

  private ConflictVehicleProperties getConflictProperties(CarReadable currentCar, CarTrailDeciderData conflictCar, double conflictAreaLength, LaneId outgoingLaneId){
    boolean isCrossConflict = conflictCar.getOutgoingLaneIdOptional().isPresent() ? (!(conflictCar.getOutgoingLaneIdOptional().get().equals(outgoingLaneId))) : true;
    double tte_a = calculateTimeToEnter(conflictCar, TimeCalculationOption.CurrentAcceleration);
    double tte_b = calculateTimeToEnter(conflictCar, TimeCalculationOption.BrakeAcceleration);
    //double ttp_z = isCrossConflict ? calculateTimeToPassableCrossing(currentCar, conflictCar, conflictAreaLength, TimeCalculationOption.CurrentSpeed) : calculateTimeToPassableMerge(currentCar, conflictCar, TimeCalculationOption.CurrentSpeed);
    double ttp_b = isCrossConflict ? calculateTimeToPassableCrossing(currentCar, conflictCar, conflictAreaLength, TimeCalculationOption.BrakeAcceleration) : calculateTimeToPassableMerge(currentCar, conflictCar, TimeCalculationOption.BrakeAcceleration);

    return new ConflictVehicleProperties(isCrossConflict, tte_a, tte_b, ttp_b, conflictCar);
  }

  private double calculateTimeToEnter(CarTrailDeciderData conflictCar, TimeCalculationOption calculationOption){
    return getTimeEstimation(conflictCar, conflictCar.getDistance(), calculationOption);
  }

  private double calculateTimeToClearCrossing(CarTrailDeciderData conflictCar, double conflictAreaLength, TimeCalculationOption calculationOption){
    return getTimeEstimation(conflictCar, conflictCar.getDistance() + conflictAreaLength + conflictCar.getLength(), calculationOption);
  }

  private double calculateTimeToPassableCrossing(CarReadable currentCar, CarTrailDeciderData conflictCar, double conflictAreaLength, TimeCalculationOption calculationOption){
    return getTimeEstimation(conflictCar, conflictCar.getDistance() + conflictAreaLength + conflictCar.getLength() + currentCar.getLength() + currentCar.getDistanceHeadway(), calculationOption);
  }

  private double calculateTimeToClearMerge(CarTrailDeciderData conflictCar, TimeCalculationOption calculationOption){
    return getTimeEstimation(conflictCar, conflictCar.getDistance() + conflictCar.getLength(), calculationOption);
  }

  private double calculateTimeToPassableMerge(CarReadable currentCar, CarTrailDeciderData conflictCar, TimeCalculationOption calculationOption){
    return getTimeEstimation(conflictCar, conflictCar.getDistance() + conflictCar.getLength() + currentCar.getLength() + currentCar.getDistanceHeadway(), calculationOption);
  }

  private double getTimeEstimation(CarTrailDeciderData conflictCar, double distance, TimeCalculationOption calculationOption){
    if(distance <= 0){
      return 0;
    }

    double acceleration = 0;
    switch(calculationOption){
      case FreeAcceleration:{
        acceleration = maxAcceleration * (1 - Math.pow(conflictCar.getSpeed() / conflictCar.getMaxSpeed(), accelerationDelta));
        break;
      }
      case CurrentAcceleration:{
        acceleration = conflictCar.getAcceleration();
        break;
      }
      case CurrentSpeed:{
        acceleration = 0;
        break;
      }
      case BrakeAcceleration:{
        acceleration = maxDeceleration;
        break;
      }
    }

    double omega = conflictCar.getSpeed() * conflictCar.getSpeed() + 2 * acceleration * distance;

    if(omega < 0){
      return Double.MAX_VALUE;
    }

    if(Math.abs(acceleration) < 0.01){
      return distance / conflictCar.getSpeed();
    }

    return (Math.sqrt(omega) - conflictCar.getSpeed()) / acceleration;
  }
}

enum TimeCalculationOption{
  CurrentSpeed, CurrentAcceleration, FreeAcceleration, BrakeAcceleration
}
