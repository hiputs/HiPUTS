package pl.edu.agh.hiputs.model.car.driver.deciders.junction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspector;
import pl.edu.agh.hiputs.model.car.driver.DriverParameters;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.IFollowingModel;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneDirection;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneOnJunction;

@Slf4j
public class TrailJunctionDecider implements JunctionDecider {

  private final CarProspector prospector;
  private final IFollowingModel followingModel;
  private final double timeDelta;
  private final double idmDelta;
  private final double conflictAreaLength;
  private final int accelerationDelta;
  private final double maxAcceleration;
  private final double maxDeceleration;
  private final int giveWayWaitCycles;
  private final int movePermanentWaitCycles;

  public TrailJunctionDecider(CarProspector prospector, IFollowingModel followingModel, DriverParameters parameters){
      this.prospector = prospector;
      this.followingModel = followingModel;
      this.timeDelta = parameters.getJunctionTimeDeltaFactor();
      this.idmDelta = parameters.getIdmDelta();
      this.conflictAreaLength = parameters.getJunctionDefaultConflictAreaLength();
      this.maxAcceleration = parameters.getIdmNormalAcceleration();
      this.maxDeceleration = parameters.getIdmNormalDeceleration();
      this.accelerationDelta = parameters.getIdmDelta();
      this.giveWayWaitCycles = parameters.getGiveWayThreshold();
      this.movePermanentWaitCycles = parameters.getMovePermanentThreshold();
  }

  @Override
  public JunctionDecision makeDecision(CarReadable managedCar, CarEnvironment environment,
      RoadStructureReader roadStructureReader) {

    if(managedCar.getCrossRoadDecisionProperties().isPresent()
        && managedCar.getCrossRoadDecisionProperties().get().getMovePermanentLaneId().isPresent()
        && managedCar.getCrossRoadDecisionProperties().get().getMovePermanentLaneId().get().equals(managedCar.getLaneId())){
      return movePermanentResult(managedCar, environment);
    }

    CarEnvironment precedingCarInfo = prospector.getPrecedingCar(managedCar, roadStructureReader);

    if(environment.getNextCrossroadId().isEmpty()){
      AccelerationDecisionResult res =
          getCrossroadAccelerationDecisionResult(managedCar.getSpeed(), managedCar.getMaxSpeed(), precedingCarInfo);
      if (res.isLocked()) {
        log.trace("Car: " + managedCar.getCarId() + " locked");
      }
      return new JunctionDecision(res.getAcceleration());
    }

    if(precedingCarInfo.getPrecedingCar().isPresent()){
      log.trace("Car: " + managedCar.getCarId() + " found preceding car: " + precedingCarInfo);
    }
    else{
      log.trace("Car: " + managedCar.getCarId() + " found no preceding car after crossroad");
    }

    if(managedCar.getCrossRoadDecisionProperties().isPresent() && managedCar.getCrossRoadDecisionProperties().get().getGiveWayVehicleId().isPresent()){
      return giveWayResult(managedCar, environment, roadStructureReader);
    }

    List<ConflictVehicleProperties> conflictVehiclesProperties = getAllConflictVehiclesProperties(managedCar, environment, roadStructureReader)
        .stream().sorted(Comparator.comparingDouble(ConflictVehicleProperties::getTte_a)).toList();

    if(conflictVehiclesProperties.isEmpty()){
      log.trace("Car: " + managedCar.getCarId() + " found no conflict vehicles");
      return getMoveOnJunctionDecision(managedCar, environment, roadStructureReader, precedingCarInfo);
    }

    CarTrailDeciderData managedCarTrailDataFreeAccel = new CarTrailDeciderData(managedCar.getSpeed(), environment.getDistance(),
        managedCar.getLength(), maxAcceleration, managedCar.getMaxSpeed(), managedCar.getCarId(), environment.getIncomingLaneId().get(), Optional.empty());

    double currentTtc_cross = calculateTimeToClearCrossing(managedCarTrailDataFreeAccel, conflictAreaLength, TimeCalculationOption.FreeAcceleration);
    double currentTtc_merge = calculateTimeToClearMerge(managedCarTrailDataFreeAccel, TimeCalculationOption.FreeAcceleration);

    double precedingTtpConstSpeed = 0;
    double precedingTtpMaxBreak = 0;
    if(precedingCarInfo.getPrecedingCar().isPresent()) {
      CarReadable precedingCar = precedingCarInfo.getPrecedingCar().get();
      // I have got distance of preceding car from current car. I need to subtract distance of current car from crossroad to get preceding car distance from crossroad.
      double precedingCarDistance = environment.getDistance() - (precedingCarInfo.getDistance() + precedingCar.getLength());

      CarTrailDeciderData precedingCarTrailData =
          new CarTrailDeciderData(precedingCar.getSpeed(), precedingCarDistance, precedingCar.getLength(), precedingCar.getAcceleration(),
              precedingCar.getMaxSpeed(), precedingCar.getCarId(), environment.getIncomingLaneId().get(), Optional.empty());

      precedingTtpConstSpeed =
          calculateTimeToPassableCrossing(managedCar, precedingCarTrailData, conflictAreaLength, TimeCalculationOption.CurrentSpeed);
      precedingTtpMaxBreak =
          calculateTimeToPassableCrossing(managedCar, precedingCarTrailData, conflictAreaLength, TimeCalculationOption.BrakeAcceleration);
    }
    ConflictVehicleProperties firstConflictVehicle = conflictVehiclesProperties.get(0);

    double currentTtc = firstConflictVehicle.isCrossConflict() ? currentTtc_cross : currentTtc_merge;
    if(timeDelta * currentTtc < firstConflictVehicle.getTte_a()){
      //drive

      if(!firstConflictVehicle.isCrossConflict()){
        double freeAcceleration = maxAcceleration * (1 - Math.pow(managedCar.getSpeed() / managedCar.getMaxSpeed(),
            idmDelta));
        double tDeltaV = Math.max((firstConflictVehicle.getCar().getSpeed() - maxDeceleration * currentTtc - managedCar.getSpeed() - freeAcceleration * currentTtc) / maxDeceleration, 0);
        if(timeDelta * (tDeltaV + currentTtc) < firstConflictVehicle.getTte_b()){
          log.trace("Car: " + managedCar.getCarId() + " accept merge conflict");
          return getMoveOnJunctionDecision(managedCar, environment, roadStructureReader, precedingCarInfo);
        }
      }
      else{
        if(timeDelta * precedingTtpConstSpeed < firstConflictVehicle.getTte_a()
            && timeDelta * precedingTtpMaxBreak < firstConflictVehicle.getTte_b()){
          log.trace("Car: " + managedCar.getCarId() + " accept cross conflict");
          return getMoveOnJunctionDecision(managedCar, environment, roadStructureReader, precedingCarInfo);
        }
        /*else{
          log.info("Car: " + managedCar.getCarId() + " is stopped by new code");
        }*/
      }
    }

    log.trace("Car: " + managedCar.getCarId() + " reject conflicts");
    return getLockedJunctionDecision(managedCar, environment, roadStructureReader, precedingCarInfo, precedingTtpMaxBreak < Double.MAX_VALUE,
        firstConflictVehicle.getCar().getFirstCarOnIncomingLaneId());
  }

  private JunctionDecision movePermanentResult(CarReadable managedCar, CarEnvironment environment) {
    CrossroadDecisionProperties lastProperties = managedCar.getCrossRoadDecisionProperties().get();

    log.trace("Car:" + managedCar.getCarId() + " continue move permanent on lane: " + managedCar.getLaneId());
    return new JunctionDecision(getCrossroadAccelerationDecisionResult(managedCar.getSpeed(), managedCar.getMaxSpeed(),
        new CarEnvironment(Optional.empty(), Optional.empty(), environment.getDistance())).getAcceleration(), lastProperties);
  }

  private JunctionDecision giveWayResult(CarReadable managedCar, CarEnvironment environment, RoadStructureReader roadStructureReader) {
    List<CarReadable> firstVehiclesOnJunction = getAllFirstVehiclesOnJunction(environment.getNextCrossroadId().get(),
        roadStructureReader);

    CrossroadDecisionProperties lastProperties = managedCar.getCrossRoadDecisionProperties().get();
    int lockCounter = 1 + lastProperties.getLockStepsCount();
    int complianceFactor = lastProperties.getComplianceFactor();
    Optional<CarId> giveWayVehicleId = Optional.empty();
    Optional<CarReadable> giveWayVehicleOptional = firstVehiclesOnJunction.stream().filter(v ->
        v.getCarId() == managedCar.getCrossRoadDecisionProperties().get().getGiveWayVehicleId().get()).findFirst();
    if(giveWayVehicleOptional.isPresent()){
      CarReadable giveWayVehicle = giveWayVehicleOptional.get();
      if(giveWayVehicle.getCrossRoadDecisionProperties().isPresent()
            && (giveWayVehicle.getCrossRoadDecisionProperties().get().getGiveWayVehicleId().isPresent()
            /*|| !giveWayVehicle.getCrossRoadDecisionProperties().get().getIsAvailableSpaceAfterCrossroad()*/)) {

        log.debug("Car:" + managedCar.getCarId() + " stop give way: " + lastProperties.getGiveWayVehicleId().get()
                + ", space: " + giveWayVehicle.getCrossRoadDecisionProperties().get().getIsAvailableSpaceAfterCrossroad()
                + ", give way free: " + giveWayVehicle.getCrossRoadDecisionProperties().get().getGiveWayVehicleId().isEmpty()
            );
        giveWayVehicleId = Optional.empty();
      }
      else{
        log.trace("Car:" + managedCar.getCarId() + " continue give way: " + lastProperties.getGiveWayVehicleId().get());
        giveWayVehicleId = managedCar.getCrossRoadDecisionProperties().get().getGiveWayVehicleId();
      }
    }
    else{
      log.debug("Car:" + managedCar.getCarId() + " finish give way: "+ lastProperties.getGiveWayVehicleId().get());
      giveWayVehicleId = Optional.empty();
    }

    CrossroadDecisionProperties decisionProperties = new CrossroadDecisionProperties(lastProperties.getBlockingCarId(),
        lockCounter, complianceFactor, lastProperties.getIsAvailableSpaceAfterCrossroad(), Optional.empty(), giveWayVehicleId);

    return new JunctionDecision(getStopAccelerationResult(managedCar.getSpeed(), managedCar.getMaxSpeed(),
        environment.getDistance() - conflictAreaLength / 2), decisionProperties);
  }

  private JunctionDecision getMoveOnJunctionDecision(CarReadable managedCar, CarEnvironment environment,
      RoadStructureReader roadStructureReader, CarEnvironment precedingCarInfo) {
    AccelerationDecisionResult res = getCrossroadAccelerationDecisionResult(managedCar.getSpeed(), managedCar.getMaxSpeed(),
        precedingCarInfo);
    if(res.isLocked()){
      return getLockedJunctionDecision(managedCar, environment, roadStructureReader, precedingCarInfo, false,
          precedingCarInfo.getPrecedingCar().get().getCarId());
    }
    else{
      return new JunctionDecision(res.getAcceleration());
    }
  }

  private JunctionDecision getLockedJunctionDecision(CarReadable managedCar, CarEnvironment environment,
      RoadStructureReader roadStructureReader, CarEnvironment precedingCarInfo, boolean haveSpaceAfterCrossroad,
      CarId lockingCarId) {

    int lockCounter = 1;
    int complianceFactor = new Random().nextInt();
    Optional<CarId> giveWayVehicleId = Optional.empty();
    if(managedCar.getCrossRoadDecisionProperties().isPresent()){
      CrossroadDecisionProperties lastProperties = managedCar.getCrossRoadDecisionProperties().get();
      lockCounter += lastProperties.getLockStepsCount();
      complianceFactor = lastProperties.getComplianceFactor();
    }

    boolean isMovedPermanent = false;
    if(lockCounter >= giveWayWaitCycles){
      List<CarReadable> firstVehiclesOnJunction = getAllFirstVehiclesOnJunction(environment.getNextCrossroadId().get(),
          roadStructureReader);
      if(firstVehiclesOnJunction.stream().findAny().isPresent()
          && firstVehiclesOnJunction.stream().allMatch(v -> v.getCrossRoadDecisionProperties().isPresent() && v.getCrossRoadDecisionProperties().get().getLockStepsCount() >= giveWayWaitCycles)){
        List<CarReadable> vehiclesWithSpaceAfterCrossroad = firstVehiclesOnJunction.stream()
            .filter(carReadable -> carReadable.getCrossRoadDecisionProperties().isPresent() && carReadable.getCrossRoadDecisionProperties().get().getGiveWayVehicleId().isEmpty()
                && carReadable.getCrossRoadDecisionProperties().get().getIsAvailableSpaceAfterCrossroad())
            .sorted((o1, o2) ->  compareLockStepCount(o1, o2)).toList();
        if(vehiclesWithSpaceAfterCrossroad.stream().findFirst().isPresent()){
          CarReadable blockedCar = vehiclesWithSpaceAfterCrossroad.stream().filter(v ->
              v.getCrossRoadDecisionProperties().get().getGiveWayVehicleId().isEmpty()).findFirst().get();
          CarId blockingCarId = blockedCar.getCrossRoadDecisionProperties().get().getBlockingCarId();
          if(blockingCarId == managedCar.getCarId()){
            giveWayVehicleId = Optional.of(blockedCar.getCarId());
            log.debug("Car: " + managedCar.getCarId() + " give way to: " + giveWayVehicleId.get());
          }
          else{
            log.trace("Car: " + managedCar.getCarId() + " not needed give way but car: " + vehiclesWithSpaceAfterCrossroad.stream().findFirst().get().getCrossRoadDecisionProperties().get().getBlockingCarId()
             + " for: " + vehiclesWithSpaceAfterCrossroad.stream().findFirst().get().getCarId());
          }
        }
        else{
          if(firstVehiclesOnJunction.stream().allMatch(v -> v.getCrossRoadDecisionProperties().get().getLockStepsCount() >= movePermanentWaitCycles)) {
            Optional<CarReadable> movePermanentOptional = firstVehiclesOnJunction.stream()
                .sorted((o1, o2) -> compareLockStepCount(o1, o2))
                .filter(car -> car.getCrossRoadDecisionProperties().get().getGiveWayVehicleId().isEmpty())
                .findFirst();
            if(movePermanentOptional.isPresent()){
              CarId movePermanentCarId = movePermanentOptional.get().getCarId();
              if (managedCar.getCarId().equals(movePermanentCarId)) {
                //Move with collision
                log.debug("Car: " + managedCar.getCarId() + " move permanent on lane: " + managedCar.getLaneId());
                isMovedPermanent = true;
              } else {
                log.trace("Car: " + managedCar.getCarId() + " not need move permanent, but: " + movePermanentCarId);
              }
            }
            else{
              log.debug("Car: " + managedCar.getCarId() + " found no candidate for move permanent");
            }
          }
        }
      }
      else if(lockCounter > movePermanentWaitCycles && firstVehiclesOnJunction.stream().noneMatch(carReadable -> carReadable.getCarId().equals(managedCar.getCarId()))
              && firstVehiclesOnJunction.stream().filter(v -> v.getCrossRoadDecisionProperties().isPresent())
              .allMatch(v -> v.getCrossRoadDecisionProperties().get().getLockStepsCount() >= movePermanentWaitCycles)){
        log.debug("Car: " + managedCar.getCarId() + " move permanent on lane: " + managedCar.getLaneId() + " cause wrong cars order on lane");
        isMovedPermanent = true;
      }
    }
    CrossroadDecisionProperties decisionProperties = new CrossroadDecisionProperties(lockingCarId,
        lockCounter, complianceFactor, haveSpaceAfterCrossroad, isMovedPermanent? Optional.of(managedCar.getLaneId()) : Optional.empty(),
        giveWayVehicleId);

    if(isMovedPermanent){
      return new JunctionDecision(getCrossroadAccelerationDecisionResult(managedCar.getSpeed(), managedCar.getMaxSpeed(),
          new CarEnvironment(Optional.empty(), Optional.empty(), precedingCarInfo.getDistance()))
          .getAcceleration(), decisionProperties);
    }
    return new JunctionDecision(getStopAccelerationResult(managedCar.getSpeed(), managedCar.getMaxSpeed(),
        environment.getDistance() - conflictAreaLength / 2), decisionProperties);
  }

  private int compareLockStepCount(CarReadable o1, CarReadable o2) {
    final int compareRes1 = Integer.compare(o2.getCrossRoadDecisionProperties().get().getLockStepsCount(),
        o1.getCrossRoadDecisionProperties().get().getLockStepsCount());
    if(compareRes1 == 0){
      return Integer.compare(o1.getCrossRoadDecisionProperties().get().getComplianceFactor(),
          o2.getCrossRoadDecisionProperties().get().getComplianceFactor());
    }
    return compareRes1;
  }

  private AccelerationDecisionResult getCrossroadAccelerationDecisionResult(double speed, double desiredSpeed, CarEnvironment precedingCarInfo) {
    if(precedingCarInfo.getPrecedingCar().isEmpty()) {
      return new AccelerationDecisionResult(followingModel.calculateAcceleration(speed, desiredSpeed, Double.MAX_VALUE, 0), false);
    }
    else{
      double acceleration = followingModel.calculateAcceleration(speed, desiredSpeed, precedingCarInfo.getDistance(), speed - precedingCarInfo.getPrecedingCar().get().getSpeed());

      final boolean isLocked = (speed + acceleration <= 0.1);
      return new AccelerationDecisionResult(acceleration, isLocked);
    }
  }

  private double getStopAccelerationResult(double speed, double desiredSpeed, double distance) {
    return followingModel.calculateAcceleration(speed, desiredSpeed, distance, speed);
  }

  private List<CarReadable> getAllFirstVehiclesOnJunction(JunctionId junctionId, RoadStructureReader roadStructureReader){
    JunctionReadable junction = roadStructureReader.getJunctionReadable(junctionId);
    List<LaneOnJunction> lanesOnJunction = junction.streamLanesOnJunction().filter(lane -> lane.getDirection().equals(LaneDirection.INCOMING)).toList();
    return prospector.getAllFirstCarsFromLanesReadable(lanesOnJunction.stream().map(LaneOnJunction::getLaneId).toList(), roadStructureReader);
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

    List<LaneOnJunction> rightLanes = prospector.getRightLanesOnJunction(lanesOnJunction, incomingLaneId, outgoingLaneId);
    List<LaneOnJunction> leftLanes = lanesOnJunction.stream().filter(lane -> !rightLanes.contains(lane)).toList();

    List<LaneId> conflictLanesId = prospector.getConflictLaneIds(lanesOnJunction, incomingLaneId, outgoingLaneId, currentCar.getCarId(),
        roadStructureReader);
    List<CarTrailDeciderData> conflictCars = prospector.getAllConflictCarsFromLanes(conflictLanesId, roadStructureReader, conflictAreaLength);
    List<ConflictVehicleProperties> conflictVehiclesProperties = conflictCars.stream().filter(car -> filterConflictCars(car, leftLanes, rightLanes, outgoingLaneId))
        .map(conflictCar -> getConflictProperties(currentCar, conflictCar, conflictAreaLength, outgoingLaneId)).toList();

    return conflictVehiclesProperties;
  }

  private boolean filterConflictCars(CarTrailDeciderData conflictCar, List<LaneOnJunction> leftLanes,
      List<LaneOnJunction> rightLanes, LaneId managedCarOutgoingLaneId){
    if(conflictCar.getOutgoingLaneIdOptional().isEmpty() || managedCarOutgoingLaneId == null) {
      return false;
    }

    if(managedCarOutgoingLaneId.equals(conflictCar.getOutgoingLaneIdOptional().get())){
      return true;
    }

    if(leftLanes.stream().anyMatch(lane -> conflictCar.getIncomingLaneId().equals(lane.getLaneId()) && lane.getDirection().equals(
        LaneDirection.INCOMING)) && leftLanes.stream().anyMatch(lane -> conflictCar.getOutgoingLaneIdOptional().get().equals(lane.getLaneId()) && lane.getDirection().equals(
        LaneDirection.OUTGOING))){
      return false;
    }
    if(rightLanes.stream().anyMatch(lane -> conflictCar.getIncomingLaneId().equals(lane.getLaneId()) && lane.getDirection().equals(
        LaneDirection.INCOMING)) && rightLanes.stream().anyMatch(lane -> conflictCar.getOutgoingLaneIdOptional().get().equals(lane.getLaneId()) && lane.getDirection().equals(
        LaneDirection.OUTGOING))){
      return false;
    }

    return true;
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
