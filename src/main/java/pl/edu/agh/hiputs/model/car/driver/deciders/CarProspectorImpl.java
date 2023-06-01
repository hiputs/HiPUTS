package pl.edu.agh.hiputs.model.car.driver.deciders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CarBasicDeciderData;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CarTrailDeciderData;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadDirection;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadOnJunction;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadSubordination;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class CarProspectorImpl implements CarProspector {

  double viewRange = 300;

  private double getViewRange(){
    return viewRange;
    //return configurationService.getConfiguration().getCarViewRange();
  }

  public CarEnvironment getPrecedingCar(CarReadable currentCar, RoadStructureReader roadStructureReader) {
    LaneReadable currentLane = roadStructureReader.getLaneReadable(currentCar.getLaneId());
    Optional<CarReadable> precedingCar = currentLane.getCarInFrontReadable(currentCar);
    double distance;
    if (precedingCar.isPresent()) {
      distance = precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength())
          - currentCar.getPositionOnLane();
    } else {
      distance = 0;
      int offset = 0;
      RoadId nextRoadId;
      RoadReadable nextRoad;
      LaneId nextLaneId;
      LaneReadable nextLane;
      while (precedingCar.isEmpty() && distance < getViewRange()) {
        Optional<RoadId> nextRoadIdOptional = currentCar.getRouteOffsetRoadId(++offset);
        if (nextRoadIdOptional.isEmpty()) {
          break;
        }
        nextRoadId = nextRoadIdOptional.get();
        distance += currentLane.getLength(); // adds previous lane length
        nextRoad = roadStructureReader.getRoadReadable(nextRoadId);
        if (nextRoad == null) {
          break;
        }

        //TODO: Change to a few possible lanes - I
        //Get Next Lanes
        List<LaneReadable> nextLanes = getNextLanes(currentLane, nextRoadId, roadStructureReader);
        if (!nextLanes.isEmpty()) {
          nextLane = nextLanes.get(ThreadLocalRandom.current().nextInt(nextLanes.size()));
          nextLaneId = nextLane.getLaneId();
        } else if (!nextRoad.getLanes().isEmpty()) {
          nextLaneId = nextRoad.getLanes().get(ThreadLocalRandom.current().nextInt(nextRoad.getLanes().size()));
          nextLane = roadStructureReader.getLaneReadable(nextLaneId);
        } else {
          log.debug("getPrecedingCar: There is no available Lanes on Road");
          break;
        }

        precedingCar = nextLane.getCarAtEntryReadable();
        currentLane = nextLane;
      }
      distance += precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength())
          - currentCar.getPositionOnLane();
    }
    return new CarEnvironment(distance, precedingCar, Optional.empty(), Optional.empty(), Optional.empty());
  }

  public CarEnvironment getPrecedingCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader){
    return getPrecedingCrossroad(currentCar, roadStructureReader, null);
  }

  public CarEnvironment getPrecedingCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader, JunctionId skipCrossroadId) {
    RoadReadable currentRoad = roadStructureReader.getRoadReadable(currentCar.getRoadId());
    LaneReadable currentLane = roadStructureReader.getLaneReadable(currentCar.getLaneId());
    JunctionId nextJunctionId = currentRoad.getOutgoingJunctionId();
    Optional<JunctionId> nextCrossroadId;
    Optional<RoadId> incomingRoadId = Optional.of(currentRoad.getRoadId());
    Optional<LaneId> incomingLaneId = Optional.of(currentLane.getLaneId());
    boolean foundPreviousCrossroad = (skipCrossroadId == null);
    double distance;
    if (nextJunctionId.isCrossroad() && foundPreviousCrossroad) {
      distance = currentRoad.getLength() - currentCar.getPositionOnLane();
    } else {
      distance = 0;
      int offset = 0;
      RoadId nextRoadId;
      RoadReadable nextRoad;
      LaneId nextLaneId;
      LaneReadable nextLane;
      while (!nextJunctionId.isCrossroad() && distance < getViewRange() && !foundPreviousCrossroad) {
        if(nextJunctionId.equals(skipCrossroadId)){
          foundPreviousCrossroad = true;
        }
        Optional<RoadId> nextRoadIdOptional = currentCar.getRouteOffsetRoadId(++offset);
        if (nextRoadIdOptional.isEmpty()) {
          break;
        }
        nextRoadId = nextRoadIdOptional.get();
        distance += currentRoad.getLength(); // adds previous lane length
        nextRoad = roadStructureReader.getRoadReadable(nextRoadId);
        if (nextRoad == null) {
          break;
        }

        //TODO: Change to a few possible lanes - I
        //Get Next Lanes
        List<LaneReadable> nextLanes = getNextLanes(currentLane, nextRoadId, roadStructureReader);
        if (!nextLanes.isEmpty()) {
          nextLane = nextLanes.get(ThreadLocalRandom.current().nextInt(nextLanes.size()));
          nextLaneId = nextLane.getLaneId();
        } else if (!nextRoad.getLanes().isEmpty()) {
          nextLaneId = nextRoad.getLanes().get(ThreadLocalRandom.current().nextInt(nextRoad.getLanes().size()));
          nextLane = roadStructureReader.getLaneReadable(nextLaneId);
        } else {
          log.debug("getPrecedingCarOrCrossroad: There is no available Lanes on Road");
          break;
        }

        nextJunctionId = nextRoad.getOutgoingJunctionId();
        incomingRoadId = Optional.of(nextRoadId);
        incomingLaneId = Optional.of(nextLaneId);
        currentRoad = nextRoad;
      }
      distance += currentRoad.getLength() - currentCar.getPositionOnLane();

    }
    double crossroadDistance = distance;
    if(nextJunctionId.isCrossroad()){
      crossroadDistance += currentRoad.getLength();
    }
    if (nextJunctionId.isCrossroad() && foundPreviousCrossroad && crossroadDistance <= getViewRange() && roadStructureReader.getJunctionReadable(nextJunctionId) != null) {
      nextCrossroadId = Optional.of(nextJunctionId);
    } else {
      nextCrossroadId = Optional.empty();
      incomingRoadId = Optional.empty();
    }
    return new CarEnvironment(distance, Optional.empty(), nextCrossroadId, incomingRoadId, incomingLaneId);
  }

  public CarEnvironment getPrecedingCarOrCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader) {
    RoadReadable currentRoad = roadStructureReader.getRoadReadable(currentCar.getRoadId());
    LaneReadable currentLane = roadStructureReader.getLaneReadable(currentCar.getLaneId());
    JunctionId nextJunctionId = currentRoad.getOutgoingJunctionId();
    Optional<CarReadable> precedingCar = currentLane.getCarInFrontReadable(currentCar);
    Optional<JunctionId> nextCrossroadId;
    Optional<RoadId> incomingRoadId = Optional.of(currentRoad.getRoadId());
    Optional<LaneId> incomingLaneId = Optional.of(currentLane.getLaneId());
    double distance;
    if (nextJunctionId.isCrossroad() || precedingCar.isPresent()) {
      distance = precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength())
          - currentCar.getPositionOnLane();
    } else {
      distance = 0;
      int offset = 0;
      RoadId nextRoadId;
      LaneId nextLaneId;
      RoadReadable nextRoad;
      LaneReadable nextLane;
      while (precedingCar.isEmpty() && !nextJunctionId.isCrossroad() && distance < getViewRange()) {
        Optional<RoadId> nextRoadIdOptional = currentCar.getRouteOffsetRoadId(++offset);
        if (nextRoadIdOptional.isEmpty()) {
          break;
        }
        nextRoadId = nextRoadIdOptional.get();
        distance += currentLane.getLength(); // adds previous lane length
        nextRoad = roadStructureReader.getRoadReadable(nextRoadId);
        if (nextRoad == null) {
          break;
        }
        nextJunctionId = nextRoad.getOutgoingJunctionId();

        //TODO: Change to a few possible lanes - I

        //Get Next Lanes
        List<LaneReadable> nextLanes = getNextLanes(currentLane, nextRoadId, roadStructureReader);
        if (!nextLanes.isEmpty()) {
          nextLane = nextLanes.get(ThreadLocalRandom.current().nextInt(nextLanes.size()));
          nextLaneId = nextLane.getLaneId();
        } else if (!nextRoad.getLanes().isEmpty()) {
          nextLaneId = nextRoad.getLanes().get(ThreadLocalRandom.current().nextInt(nextRoad.getLanes().size()));
          nextLane = roadStructureReader.getLaneReadable(nextLaneId);
        } else {
          log.debug("getPrecedingCarOrCrossroad: There is no available Lanes on Road");
          break;
        }

        precedingCar = nextLane.getCarAtEntryReadable();
        incomingRoadId = Optional.of(nextRoadId);
        incomingLaneId = Optional.of(nextLaneId);
        currentLane = nextLane;
        currentRoad = nextRoad;
      }
      distance += precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength())
          - currentCar.getPositionOnLane();
    }
    double crossroadDistance = distance;
    if(precedingCar.isPresent() && nextJunctionId.isCrossroad()){
      crossroadDistance += currentLane.getLength() - precedingCar.get().getPositionOnLane();
    }
    if (nextJunctionId.isCrossroad() && crossroadDistance <= getViewRange() && roadStructureReader.getJunctionReadable(nextJunctionId) != null) {
      nextCrossroadId = Optional.of(nextJunctionId);
    } else {
      nextCrossroadId = Optional.empty();
      incomingRoadId = Optional.empty();
      incomingLaneId = Optional.empty();
    }
    return new CarEnvironment(distance, precedingCar, nextCrossroadId, incomingRoadId, incomingLaneId);
  }

  public List<LaneReadable> getNextLanes(LaneReadable currentLane, RoadId nextRoadId, RoadStructureReader roadStructureReader) {
    return currentLane.getLaneSuccessors()
        .stream()
        .map(roadStructureReader::getLaneReadable)
        .filter(lane -> lane.getRoadId() == nextRoadId)
        .toList();

  }

  public List<RoadOnJunction> getRightRoadsOnJunction(List<RoadOnJunction> roadOnJunctions, RoadId incomingRoadId, RoadId outgoingRoadId){
    Optional<RoadOnJunction> incomingRoadOnJunctionOpt = roadOnJunctions.stream().filter(
        roadOnJunction -> roadOnJunction.getRoadId() == incomingRoadId
            && roadOnJunction.getDirection().equals(RoadDirection.INCOMING)).findFirst();
    if(incomingRoadOnJunctionOpt.isEmpty()){
      return new ArrayList<>();
    }
    RoadOnJunction incomingRoadOnJunction = incomingRoadOnJunctionOpt.get();
    RoadOnJunction outgoingRoadOnJunction = roadOnJunctions.stream().filter(
        roadOnJunction -> roadOnJunction.getRoadId() == outgoingRoadId
            && roadOnJunction.getDirection().equals(RoadDirection.OUTGOING)).findFirst().or(()->Optional.of(
        incomingRoadOnJunction)).get();

    List<RoadOnJunction> rightRoads;
    rightRoads = roadOnJunctions.stream()
        .filter(roadOnJunction -> roadOnJunction.getDirection().equals(RoadDirection.INCOMING)
            && isRoadOnJunctionIndexInRange(roadOnJunction.getRoadIndexOnJunction(), incomingRoadOnJunction.getRoadIndexOnJunction(), outgoingRoadOnJunction.getRoadIndexOnJunction())).toList();
    return rightRoads;
  }

  public List<RoadId> getConflictRoadIds(List<RoadOnJunction> roadOnJunctions, RoadId incomingRoadId,
      RoadId outgoingRoadId, CarId currentCarId, RoadStructureReader roadStructureReader){
    Optional<RoadOnJunction> incomingRoadOnJunctionOpt = roadOnJunctions.stream().filter(
        roadOnJunction -> roadOnJunction.getRoadId() == incomingRoadId
            && roadOnJunction.getDirection().equals(RoadDirection.INCOMING)).findFirst();
    if(incomingRoadOnJunctionOpt.isEmpty()){
      return new ArrayList<>();
    }
    RoadOnJunction incomingRoadOnJunction = incomingRoadOnJunctionOpt.get();
    RoadOnJunction outgoingRoadOnJunction = roadOnJunctions.stream().filter(
        roadOnJunction -> roadOnJunction.getRoadId() == outgoingRoadId
            && roadOnJunction.getDirection().equals(RoadDirection.OUTGOING)).findFirst().or(()->Optional.of(
        incomingRoadOnJunction)).get();

    Stream<RoadOnJunction> conflictRoads;
    conflictRoads = roadOnJunctions.stream()
        .filter(roadOnJunction -> roadOnJunction.getDirection().equals(RoadDirection.INCOMING)
            && isRoadOnJunctionIndexInRange(roadOnJunction.getRoadIndexOnJunction(), incomingRoadOnJunction.getRoadIndexOnJunction(), outgoingRoadOnJunction.getRoadIndexOnJunction())).toList().stream();

    if(incomingRoadOnJunction.getSubordination().equals(RoadSubordination.MAIN_ROAD)){
      conflictRoads = conflictRoads
          .filter(roadOnJunction -> roadOnJunction.getSubordination().equals(RoadSubordination.MAIN_ROAD));
    }
    else{
      conflictRoads = Stream.concat(conflictRoads,
              roadOnJunctions.stream()
                  .filter(roadOnJunction -> roadOnJunction.getDirection().equals(RoadDirection.INCOMING)
                      && roadOnJunction.getSubordination().equals(RoadSubordination.MAIN_ROAD)))
          .distinct();
    }


    return conflictRoads.map(RoadOnJunction::getRoadId).filter(road -> filterActiveRoad(road, currentCarId, roadStructureReader)).toList();
  }

  private boolean filterActiveRoad(RoadId roadId, CarId currentCarId, RoadStructureReader roadStructureReader){
    RoadReadable road = roadId.getReadable(roadStructureReader);
    // Optional<CarReadable> conflictCarOptional = Optional.empty();
    if (road != null){
      List<Optional<CarReadable>> conflictCarsOptional = roadStructureReader.getLanesReadableFromRoadId(roadId)
          .stream()
          .map(l -> l.streamCarsFromExitReadable().findFirst())
          .toList();

      for (Optional<CarReadable> car: conflictCarsOptional){
        if (car.isPresent() && (car.get().getCrossRoadDecisionProperties().isEmpty()
            || car.get().getCrossRoadDecisionProperties().get().getGiveWayVehicleId().isEmpty()
            || !car.get().getCrossRoadDecisionProperties().get().getGiveWayVehicleId().get().equals(currentCarId))) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isRoadOnJunctionIndexInRange(int index, int incoming, int outgoing){
    if(incoming <= outgoing){
      return index > incoming && index < outgoing;
    }
    else{
      return index > incoming || index < outgoing;
    }
  }

  public List<CarBasicDeciderData> getFirstCarsFromRoads(List<RoadId> conflictRoads, RoadStructureReader roadStructureReader){
    List<CarBasicDeciderData> conflictCars = new ArrayList<>();
    for (RoadId roadId: conflictRoads) {
      RoadReadable road = roadId.getReadable(roadStructureReader);

      if(road != null) {

        List<LaneReadable> lanes = roadStructureReader.getLanesReadableFromRoadId(roadId);
        for(LaneReadable lane: lanes) {
          Optional<CarReadable> conflictCarOptional = lane.streamCarsFromExitReadable().findFirst();
          if (conflictCarOptional.isPresent()) {
            CarReadable conflictCar = conflictCarOptional.get();
            double distance = lane.getLength() - conflictCar.getPositionOnLane();
            conflictCars.add(new CarBasicDeciderData(conflictCar.getSpeed(), distance, conflictCar.getLength()));
          }
        }
      }
    }
    return conflictCars;
  }

  public List<CarReadable> getAllFirstCarsFromRoads(List<RoadId> roads, RoadStructureReader roadStructureReader){
    List<CarReadable> cars = new ArrayList<>();

    for (RoadId roadId: roads) {
      List<LaneReadable> lanes = roadStructureReader.getLanesReadableFromRoadId(roadId);

      for (LaneReadable lane: lanes){
        List<CarReadable> carsFromExit = lane.streamCarsFromExitReadable().toList();
        if (!carsFromExit.isEmpty()) {
          cars.add(carsFromExit.stream().findFirst().get());
        }
      }
    }
    return cars;
  }
  public List<CarTrailDeciderData> getAllConflictCarsFromRoads(List<RoadId> conflictRoads, RoadStructureReader roadStructureReader, double conflictAreaLength){
    List<CarTrailDeciderData> conflictCars = new ArrayList<>();

    for (RoadId roadId: conflictRoads) {
      RoadReadable road = roadId.getReadable(roadStructureReader);
      if(road != null) {
        double maxSpeed = Double.MAX_VALUE;//road.getMaxSpeed(); // #TODO Get max speed from road when it will be available

        List<LaneReadable> lanes = roadStructureReader.getLanesReadableFromRoadId(roadId);
        for (LaneReadable l: lanes) {
          Stream<CarReadable> carsFromExit = l.streamCarsFromExitReadable();

          if (carsFromExit.findAny().isPresent()) {
            final CarId finalFirstCarId = carsFromExit.findFirst().get().getCarId();

            carsFromExit.forEach(conflictCar -> {
              double distance = road.getLength() - conflictCar.getPositionOnLane() - conflictAreaLength / 2;
              conflictCars.add(new CarTrailDeciderData(conflictCar.getSpeed(), distance, conflictCar.getLength(),
                  conflictCar.getAcceleration(), Math.min(maxSpeed, conflictCar.getMaxSpeed()), finalFirstCarId, road.getRoadId(), conflictCar.getRouteOffsetRoadId(1)));
            });
          }
        }
        }

      /*if(road.getLength() < getViewRange()){ //does not give any improvement
        double distanceAdd = road.getLength();
        JunctionId previousJunctionId = road.getIncomingJunctionId();
        JunctionReadable junction = roadStructureReader.getJunctionReadable(previousJunctionId);
        List<LaneId> lanesBefore = junction.streamLanesOnJunction().filter(l -> l.getDirection().equals(LaneDirection.INCOMING)).map(l->l.getLaneId()).toList();
        for (LaneId prevLaneId: lanesBefore) {
          LaneReadable prevLane = prevLaneId.getReadable(roadStructureReader);
          maxSpeed = Double.MAX_VALUE;//road.getMaxSpeed();
          carsFromExit = prevLane.streamCarsFromExitReadable().toList();
          if (!carsFromExit.isEmpty()) {
            if (firstCarId == null) {
              firstCarId = carsFromExit.stream().findFirst().get().getCarId();
            }
            for (CarReadable conflictCar : carsFromExit) {
              double distance =
                  distanceAdd + prevLane.getLength() - conflictCar.getPositionOnLane() - conflictAreaLength / 2;
              conflictCars.add(new CarTrailDeciderData(conflictCar.getSpeed(), distance, conflictCar.getLength(),
                  conflictCar.getAcceleration(), Math.min(maxSpeed, conflictCar.getMaxSpeed()), firstCarId, road.getLaneId(), conflictCar.getRouteOffsetLaneId(1)));
            }
          }
        }
      }*/
    }
    return conflictCars;
  }

  public RoadId getNextOutgoingRoad(CarReadable car, JunctionId junctionId, RoadStructureReader roadStructureReader){
    RoadId outgoingRoadId = null;
    int offset = 0;
    RoadId tmpRoadId;
    do{
      Optional<RoadId> nextRoadIdOptional = car.getRouteOffsetRoadId(offset++);
      if(nextRoadIdOptional.isPresent()) {
        tmpRoadId = nextRoadIdOptional.get();
        RoadReadable tmpRoad = tmpRoadId.getReadable(roadStructureReader);
        if (tmpRoad != null && tmpRoad.getIncomingJunctionId().equals(junctionId)) {
          outgoingRoadId = tmpRoadId;
        }
      }
      else{
        tmpRoadId = null;
      }
    }
    while(outgoingRoadId == null && tmpRoadId != null);
    return outgoingRoadId;
  }
}
