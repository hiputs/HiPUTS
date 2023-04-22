package pl.edu.agh.hiputs.model.car.driver.deciders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CarBasicDeciderData;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CarTrailDeciderData;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadDirection;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadOnJunction;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadSubordination;

@NoArgsConstructor
@AllArgsConstructor
public class CarProspectorImpl implements CarProspector {

  double viewRange = 300;

  private double getViewRange(){
    return viewRange;
    //return configurationService.getConfiguration().getCarViewRange();
  }

  public CarEnvironment getPrecedingCar(CarReadable currentCar, RoadStructureReader roadStructureReader) {
    RoadReadable currentRoad = roadStructureReader.getRoadReadable(currentCar.getRoadId());
    Optional<CarReadable> precedingCar = currentRoad.getCarInFrontReadable(currentCar);
    double distance;
    if (precedingCar.isPresent()) {
      distance = precedingCar.map(car -> car.getPositionOnRoad() - car.getLength()).orElse(currentRoad.getLength())
          - currentCar.getPositionOnRoad();
    } else {
      distance = 0;
      int offset = 0;
      RoadId nextRoadId;
      RoadReadable nextRoad;
      while (precedingCar.isEmpty() && distance < getViewRange()) {
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
        precedingCar = nextRoad.getCarAtEntryReadable();
        currentRoad = nextRoad;
      }
      distance += precedingCar.map(car -> car.getPositionOnRoad() - car.getLength()).orElse(currentRoad.getLength())
          - currentCar.getPositionOnRoad();
    }
    return new CarEnvironment(distance, precedingCar, Optional.empty(), Optional.empty());
  }

  public CarEnvironment getPrecedingCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader){
    return getPrecedingCrossroad(currentCar, roadStructureReader, null);
  }

  public CarEnvironment getPrecedingCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader, JunctionId skipCrossroadId) {
    RoadReadable currentRoad = roadStructureReader.getRoadReadable(currentCar.getRoadId());
    JunctionId nextJunctionId = currentRoad.getOutgoingJunctionId();
    Optional<JunctionId> nextCrossroadId;
    Optional<RoadId> incomingRoadId = Optional.of(currentRoad.getRoadId());
    boolean foundPreviousCrossroad = (skipCrossroadId == null);
    double distance;
    if (nextJunctionId.isCrossroad() && foundPreviousCrossroad) {
      distance = currentRoad.getLength() - currentCar.getPositionOnRoad();
    } else {
      distance = 0;
      int offset = 0;
      RoadId nextRoadId;
      RoadReadable nextRoad;
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
        nextJunctionId = nextRoad.getOutgoingJunctionId();
        incomingRoadId = Optional.of(nextRoadId);
        currentRoad = nextRoad;
      }
      distance += currentRoad.getLength() - currentCar.getPositionOnRoad();

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
    return new CarEnvironment(distance, Optional.empty(), nextCrossroadId, incomingRoadId);
  }

  public CarEnvironment getPrecedingCarOrCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader) {
    RoadReadable currentRoad = roadStructureReader.getRoadReadable(currentCar.getRoadId());
    JunctionId nextJunctionId = currentRoad.getOutgoingJunctionId();
    Optional<CarReadable> precedingCar = currentRoad.getCarInFrontReadable(currentCar);
    Optional<JunctionId> nextCrossroadId;
    Optional<RoadId> incomingRoadId = Optional.of(currentRoad.getRoadId());
    double distance;
    if (nextJunctionId.isCrossroad() || precedingCar.isPresent()) {
      distance = precedingCar.map(car -> car.getPositionOnRoad() - car.getLength()).orElse(currentRoad.getLength())
          - currentCar.getPositionOnRoad();
    } else {
      distance = 0;
      int offset = 0;
      RoadId nextRoadId;
      RoadReadable nextRoad;
      while (precedingCar.isEmpty() && !nextJunctionId.isCrossroad() && distance < getViewRange()) {
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
        nextJunctionId = nextRoad.getOutgoingJunctionId();
        precedingCar = nextRoad.getCarAtEntryReadable();
        incomingRoadId = Optional.of(nextRoadId);
        currentRoad = nextRoad;
      }
      distance += precedingCar.map(car -> car.getPositionOnRoad() - car.getLength()).orElse(currentRoad.getLength())
          - currentCar.getPositionOnRoad();
    }
    double crossroadDistance = distance;
    if(precedingCar.isPresent() && nextJunctionId.isCrossroad()){
      crossroadDistance += currentRoad.getLength() - precedingCar.get().getPositionOnRoad();
    }
    if (nextJunctionId.isCrossroad() && crossroadDistance <= getViewRange() && roadStructureReader.getJunctionReadable(nextJunctionId) != null) {
      nextCrossroadId = Optional.of(nextJunctionId);
    } else {
      nextCrossroadId = Optional.empty();
      incomingRoadId = Optional.empty();
    }
    return new CarEnvironment(distance, precedingCar, nextCrossroadId, incomingRoadId);
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

    if(incomingRoadOnJunction.getSubordination().equals(RoadSubordination.NOT_SUBORDINATE)){
      conflictRoads = conflictRoads.filter(roadOnJunction -> roadOnJunction.getSubordination().equals(RoadSubordination.NOT_SUBORDINATE)).toList().stream();
    }
    else{
      conflictRoads = Stream.concat(conflictRoads, roadOnJunctions.stream()
          .filter(roadOnJunction -> roadOnJunction.getDirection().equals(RoadDirection.INCOMING)
              && roadOnJunction.getSubordination().equals(RoadSubordination.NOT_SUBORDINATE))).distinct().toList().stream();
    }


    return conflictRoads.map(RoadOnJunction::getRoadId).filter(road -> filterActiveRoad(road, currentCarId, roadStructureReader)).toList();
  }

  private boolean filterActiveRoad(RoadId roadId, CarId currentCarId, RoadStructureReader roadStructureReader){
    RoadReadable road = roadId.getReadable(roadStructureReader);
    Optional<CarReadable> conflictCarOptional = (road != null) ? road.streamCarsFromExitReadable().findFirst() : Optional.empty();
    return conflictCarOptional.isPresent() && (conflictCarOptional.get().getCrossRoadDecisionProperties().isEmpty()
        || conflictCarOptional.get().getCrossRoadDecisionProperties().get().getGiveWayVehicleId().isEmpty()
        || !conflictCarOptional.get().getCrossRoadDecisionProperties().get().getGiveWayVehicleId().get().equals(currentCarId));
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
        Optional<CarReadable> conflictCarOptional = road.streamCarsFromExitReadable().findFirst();
        if (conflictCarOptional.isPresent()) {
          CarReadable conflictCar = conflictCarOptional.get();
          double distance = road.getLength() - conflictCar.getPositionOnRoad();
          conflictCars.add(new CarBasicDeciderData(conflictCar.getSpeed(), distance, conflictCar.getLength()));
        }
      }
    }
    return conflictCars;
  }

  public List<CarReadable> getAllFirstCarsFromRoadsReadable(List<RoadId> roads, RoadStructureReader roadStructureReader){
    List<CarReadable> cars = new ArrayList<>();
    for (RoadId roadId: roads) {
      RoadReadable road = roadId.getReadable(roadStructureReader);

      if (road == null || road.numberOfCars() == 0) {
        continue;
      }

      List<CarReadable> carsFromExit = road.streamCarsFromExitReadable().toList();
      if (!carsFromExit.isEmpty()) {
        cars.add(carsFromExit.stream().findFirst().get());
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
        List<CarReadable> carsFromExit = road.streamCarsFromExitReadable().toList();
        CarId firstCarId = null;
        if (!carsFromExit.isEmpty()) {
          firstCarId = carsFromExit.stream().findFirst().get().getCarId();
          for (CarReadable conflictCar : carsFromExit) {
            double distance = road.getLength() - conflictCar.getPositionOnRoad() - conflictAreaLength / 2;
            conflictCars.add(new CarTrailDeciderData(conflictCar.getSpeed(), distance, conflictCar.getLength(),
                conflictCar.getAcceleration(), Math.min(maxSpeed, conflictCar.getMaxSpeed()), firstCarId, road.getRoadId(), conflictCar.getRouteOffsetRoadId(1)));
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
