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
import pl.edu.agh.hiputs.model.car.driver.deciders.CarPrecedingEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.ICarFollowingModel;
import pl.edu.agh.hiputs.model.car.driver.deciders.lights.TrafficLightsDecider;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadDirection;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadOnJunction;

@Slf4j
public class TrailJunctionDecider implements JunctionDecider {

  private final CarProspector prospector;
  private final TrafficLightsDecider trafficLightsDecider;
  private final ICarFollowingModel carFollowingModel;
  private final double timeDelta;
  private final double idmDelta;
  private final double conflictAreaLength;
  private final int accelerationDelta;
  private final double maxAcceleration;
  private final double maxDeceleration;
  private final int giveWayWaitCycles;
  private final int movePermanentWaitCycles;

  public TrailJunctionDecider(CarProspector prospector, ICarFollowingModel carFollowingModel,
      TrafficLightsDecider trafficLightsDecider, DriverParameters parameters) {
      this.prospector = prospector;
    this.carFollowingModel = carFollowingModel;
    this.trafficLightsDecider = trafficLightsDecider;
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
  public JunctionDecision makeDecision(CarReadable managedCar, CarPrecedingEnvironment environment,
      RoadStructureReader roadStructureReader) {

    // check if crossroad decision for car is calculated and
    if(managedCar.getCrossRoadDecisionProperties().isPresent()
        && managedCar.getCrossRoadDecisionProperties().get().getMovePermanentRoadId().isPresent()
        && managedCar.getCrossRoadDecisionProperties().get().getMovePermanentRoadId().get().equals(managedCar.getRoadId())){
      return movePermanentResult(managedCar, environment);
    }

    // check if there is car available after crossroad
    CarPrecedingEnvironment precedingCarInfo = prospector.getPrecedingCar(managedCar, roadStructureReader);

    // there is no crossroad in view area
    if(environment.getNextCrossroadId().isEmpty()){
      AccelerationDecisionResult res =
          getCrossroadAccelerationDecisionResult(managedCar.getSpeed(), managedCar.getMaxSpeed(), precedingCarInfo);
      if (res.isLocked()) {
        log.trace("Car: " + managedCar.getCarId() + " locked");
      }
      return new JunctionDecision(res.getAcceleration());
    }

    // no decision made earlier && car found a crossroad nearby => decision dependent on traffic lights
    Optional<JunctionDecision> trafficLightsDecision = trafficLightsDecider.tryToMakeDecision(
        managedCar, environment, roadStructureReader);
    if (trafficLightsDecision.isPresent()) {
      return trafficLightsDecision.get();
    }

    // if there is car after crossroad
    if(precedingCarInfo.getPrecedingCar().isPresent()){
      log.trace("Car: " + managedCar.getCarId() + " found preceding car: " + precedingCarInfo);
    }
    else{
      log.trace("Car: " + managedCar.getCarId() + " found no preceding car after crossroad");
    }

    // if there is crossroad decision and there are cars to give way
    if (managedCar.getCrossRoadDecisionProperties().isPresent() && managedCar.getCrossRoadDecisionProperties()
        .get()
        .getGiveWayVehicleId()
        .isPresent()) {
      return giveWayResult(managedCar, environment, roadStructureReader);
    }

    List<ConflictVehicleProperties> conflictVehiclesProperties = getAllConflictVehiclesProperties(managedCar, environment, roadStructureReader)
        .stream().sorted(Comparator.comparingDouble(ConflictVehicleProperties::getTte_a)).toList();

    if(conflictVehiclesProperties.isEmpty()){
      log.trace("Car: " + managedCar.getCarId() + " found no conflict vehicles");
      return getMoveOnJunctionDecision(managedCar, environment, roadStructureReader, precedingCarInfo);
    }

    CarTrailDeciderData managedCarTrailDataFreeAccel = new CarTrailDeciderData(managedCar.getSpeed(), environment.getDistance(),
        managedCar.getLength(), maxAcceleration, managedCar.getMaxSpeed(), managedCar.getCarId(), environment.getIncomingRoadId().get(), Optional.empty());

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
              precedingCar.getMaxSpeed(), precedingCar.getCarId(), environment.getIncomingRoadId().get(), Optional.empty());

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
        firstConflictVehicle.getCar().getFirstCarOnIncomingRoadId());
  }

  private JunctionDecision movePermanentResult(CarReadable managedCar, CarPrecedingEnvironment environment) {
    CrossroadDecisionProperties lastProperties = managedCar.getCrossRoadDecisionProperties().get();

    log.trace("Car:" + managedCar.getCarId() + " continue move permanent on road: " + managedCar.getRoadId());

    CarPrecedingEnvironment env =
        new CarPrecedingEnvironment(Optional.empty(), Optional.empty(), environment.getDistance());

    AccelerationDecisionResult decisionResult =
        getCrossroadAccelerationDecisionResult(managedCar.getSpeed(), managedCar.getMaxSpeed(), env);

    return new JunctionDecision(decisionResult.getAcceleration(), lastProperties);
  }

  private JunctionDecision giveWayResult(CarReadable managedCar, CarPrecedingEnvironment environment,
      RoadStructureReader roadStructureReader) {
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
        lockCounter, complianceFactor, lastProperties.getIsAvailableSpaceAfterCrossroad(), Optional.empty(),
        Optional.empty(), giveWayVehicleId);

    return new JunctionDecision(getStopAccelerationResult(managedCar.getSpeed(), managedCar.getMaxSpeed(),
        environment.getDistance() - conflictAreaLength / 2), decisionProperties);
  }

  private JunctionDecision getMoveOnJunctionDecision(CarReadable managedCar, CarPrecedingEnvironment environment,
      RoadStructureReader roadStructureReader, CarPrecedingEnvironment precedingCarInfo) {
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

  private JunctionDecision getLockedJunctionDecision(CarReadable managedCar, CarPrecedingEnvironment environment,
      RoadStructureReader roadStructureReader, CarPrecedingEnvironment precedingCarInfo,
      boolean haveSpaceAfterCrossroad,
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

      //check are vehicle after crossroad
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
                log.debug("Car: " + managedCar.getCarId() + " move permanent on road: " + managedCar.getRoadId());
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
        //no vehicles on crossroad, check if there are others vehicles and if there are crossroad decision properties
        // calculated
        //and cars with those decisions are able to move due to lock steps
        log.debug("Car: " + managedCar.getCarId() + " move permanent on road: " + managedCar.getRoadId() + " cause wrong cars order on road");
        isMovedPermanent = true;
      }
    }

    CrossroadDecisionProperties decisionProperties =
        new CrossroadDecisionProperties(lockingCarId, lockCounter, complianceFactor, haveSpaceAfterCrossroad,
            isMovedPermanent ? Optional.of(managedCar.getRoadId()) : Optional.empty(),
            isMovedPermanent ? Optional.of(managedCar.getLaneId()) : Optional.empty(),
        giveWayVehicleId);

    if(isMovedPermanent){
      return new JunctionDecision(getCrossroadAccelerationDecisionResult(managedCar.getSpeed(), managedCar.getMaxSpeed(),
          new CarPrecedingEnvironment(Optional.empty(), Optional.empty(), precedingCarInfo.getDistance()))
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

  private AccelerationDecisionResult getCrossroadAccelerationDecisionResult(double speed, double desiredSpeed,
      CarPrecedingEnvironment precedingCarInfo) {
    if(precedingCarInfo.getPrecedingCar().isEmpty()) {
      return new AccelerationDecisionResult(
          carFollowingModel.calculateAcceleration(speed, desiredSpeed, Double.MAX_VALUE, 0), false);
    }
    else{
      double acceleration = carFollowingModel.calculateAcceleration(speed, desiredSpeed, precedingCarInfo.getDistance(),
          speed - precedingCarInfo.getPrecedingCar().get().getSpeed());

      final boolean isLocked = (speed + acceleration <= 0.1);
      return new AccelerationDecisionResult(acceleration, isLocked);
    }
  }

  private double getStopAccelerationResult(double speed, double desiredSpeed, double distance) {
    return carFollowingModel.calculateAcceleration(speed, desiredSpeed, distance, speed);
  }

  private List<CarReadable> getAllFirstVehiclesOnJunction(JunctionId junctionId, RoadStructureReader roadStructureReader){
    JunctionReadable junction = roadStructureReader.getJunctionReadable(junctionId);
    List<RoadOnJunction> roadsOnJunction = junction.streamRoadOnJunction().filter(road -> road.getDirection().equals(
        RoadDirection.INCOMING)).toList();
    return prospector.getAllFirstCarsFromRoads(roadsOnJunction.stream().map(RoadOnJunction::getRoadId).toList(),
        roadStructureReader);
  }

  private List<ConflictVehicleProperties> getAllConflictVehiclesProperties(CarReadable currentCar,
      CarPrecedingEnvironment environment, RoadStructureReader roadStructureReader) {
    if(environment.getNextCrossroadId().isEmpty() || environment.getIncomingRoadId().isEmpty()){
      return new ArrayList<>();
    }

    JunctionId nextJunctionId = environment.getNextCrossroadId().get();
    JunctionReadable junction = roadStructureReader.getJunctionReadable(nextJunctionId);
    List<RoadOnJunction> roadOnJunctions = junction.streamRoadOnJunction().toList();
    RoadId incomingRoadId = environment.getIncomingRoadId().get();
    RoadId outgoingRoadId = prospector.getNextOutgoingRoad(currentCar, nextJunctionId, roadStructureReader);

    List<RoadOnJunction> rightRoads = prospector.getRightRoadsOnJunction(roadOnJunctions, incomingRoadId, outgoingRoadId);
    List<RoadOnJunction> leftRoads = roadOnJunctions.stream().filter(road -> !rightRoads.contains(road)).toList();

    List<RoadId> conflictRoadsId = prospector.getConflictRoadIds(roadOnJunctions, incomingRoadId, outgoingRoadId, currentCar.getCarId(),
        roadStructureReader);
    List<CarTrailDeciderData> conflictCars = prospector.getAllConflictCarsFromRoads(conflictRoadsId, roadStructureReader, conflictAreaLength);
    List<ConflictVehicleProperties> conflictVehiclesProperties = conflictCars.stream()
        .filter(car -> filterConflictCars(car, leftRoads, rightRoads, outgoingRoadId))
        .map(conflictCar -> getConflictProperties(currentCar, conflictCar, conflictAreaLength, outgoingRoadId))
        .toList();

    return conflictVehiclesProperties;
  }

  private boolean filterConflictCars(CarTrailDeciderData conflictCar, List<RoadOnJunction> leftRoads,
      List<RoadOnJunction> rightRoads, RoadId managedCarOutgoingRoadId){
    if(conflictCar.getOutgoingRoadIdOptional().isEmpty() || managedCarOutgoingRoadId == null) {
      return false;
    }

    if(managedCarOutgoingRoadId.equals(conflictCar.getOutgoingRoadIdOptional().get())){
      return true;
    }

    if (leftRoads.stream()
        .anyMatch(road -> conflictCar.getIncomingRoadId().equals(road.getRoadId()) && road.getDirection()
            .equals(RoadDirection.INCOMING)) && leftRoads.stream()
        .anyMatch(road -> conflictCar.getOutgoingRoadIdOptional().get().equals(road.getRoadId()) && road.getDirection()
            .equals(RoadDirection.OUTGOING))) {
      return false;
    }
    if (rightRoads.stream()
        .anyMatch(road -> conflictCar.getIncomingRoadId().equals(road.getRoadId()) && road.getDirection()
            .equals(RoadDirection.INCOMING)) && rightRoads.stream()
        .anyMatch(road -> conflictCar.getOutgoingRoadIdOptional().get().equals(road.getRoadId()) && road.getDirection()
            .equals(
        RoadDirection.OUTGOING))){
      return false;
    }

    return true;
  }

  private ConflictVehicleProperties getConflictProperties(CarReadable currentCar, CarTrailDeciderData conflictCar, double conflictAreaLength, RoadId outgoingRoadId){
    boolean isCrossConflict = conflictCar.getOutgoingRoadIdOptional().isPresent() ? (!(conflictCar.getOutgoingRoadIdOptional().get().equals(outgoingRoadId))) : true;
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
