package pl.edu.agh.hiputs.model.car.driver.deciders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CarBasicDeciderData;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneDirection;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneOnJunction;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneSubordination;

@NoArgsConstructor
@AllArgsConstructor
@Service
public class CarProspectorImpl implements CarProspector {

  //private ConfigurationService configurationService;
  int viewRange = 300;//#TODO load it from spring ConfigurationService

  private int getViewRange(){
    return viewRange;
    //return configurationService.getConfiguration().getCarViewRange();
  }

  public CarEnvironment getPrecedingCarOrCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader) {
    LaneReadable currentLane = roadStructureReader.getLaneReadable(currentCar.getLaneId());
    JunctionId nextJunctionId = currentLane.getOutgoingJunctionId();
    Optional<CarReadable> precedingCar = currentLane.getCarInFrontReadable(currentCar);
    Optional<JunctionId> nextCrossroadId;
    Optional<LaneId> incomingLaneId = Optional.of(currentLane.getLaneId());
    double distance;
    if (nextJunctionId.isCrossroad() || precedingCar.isPresent()) {
      distance = precedingCar.map(car -> car.getPositionOnLane() - car.getLength()).orElse(currentLane.getLength())
          - currentCar.getPositionOnLane();
    } else {
      distance = 0;
      int offset = 0;
      LaneId nextLaneId;
      LaneReadable nextLane;
      while (precedingCar.isEmpty() && !nextJunctionId.isCrossroad() && distance < getViewRange()) {
        Optional<LaneId> nextLaneIdOptional = currentCar.getRouteOffsetLaneId(++offset);
        if (nextLaneIdOptional.isEmpty()) {
          break;
        }
        nextLaneId = nextLaneIdOptional.get();
        distance += currentLane.getLength(); // adds previous lane length
        nextLane = roadStructureReader.getLaneReadable(nextLaneId);
        if (nextLane == null) {
          break;
        }
        nextJunctionId = nextLane.getOutgoingJunctionId();
        precedingCar = nextLane.getCarAtEntryReadable();
        incomingLaneId = Optional.of(nextLaneId);
        currentLane = nextLane;
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
      incomingLaneId = Optional.empty();
    }
    return new CarEnvironment(distance, precedingCar, nextCrossroadId, incomingLaneId);
  }

  public List<LaneId> getConflictLaneIds(List<LaneOnJunction> lanesOnJunction, LaneId incomingLaneId, LaneId outgoingLaneId){
    Optional<LaneOnJunction> incomingLaneOnJunctionOpt = lanesOnJunction.stream().filter(
        laneOnJunction -> laneOnJunction.getLaneId() == incomingLaneId
            && laneOnJunction.getDirection().equals(LaneDirection.INCOMING)).findFirst();
    if(incomingLaneOnJunctionOpt.isEmpty()){
      return new ArrayList<>();
    }
    LaneOnJunction incomingLaneOnJunction = incomingLaneOnJunctionOpt.get();
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
    return conflictLanes.map(LaneOnJunction::getLaneId).toList();
  }

  private boolean isLaneOnJunctionIndexInRange(int index, int incoming, int outgoing){
    if(incoming <= outgoing){
      return index > incoming && index < outgoing;
    }
    else{
      return index > incoming || index < outgoing;
    }
  }

  public List<CarBasicDeciderData> getConflictCars(List<LaneId> conflictLanes, RoadStructureReader roadStructureReader){
    List<CarBasicDeciderData> conflictCars = new ArrayList<>();
    for (LaneId laneId: conflictLanes) {
      LaneReadable lane = laneId.getReadable(roadStructureReader);
      Optional<CarReadable> conflictCarOptional = lane.streamCarsFromExitReadable().findFirst();
      if(conflictCarOptional.isPresent()){
        CarReadable conflictCar = conflictCarOptional.get();
        double distance = lane.getLength() - conflictCar.getPositionOnLane();
        conflictCars.add(new CarBasicDeciderData(conflictCar.getSpeed(), distance, conflictCar.getLength()));
      }
    }
    return conflictCars;
  }

  public LaneId getNextOutgoingLane(CarReadable car, JunctionId junctionId, RoadStructureReader roadStructureReader){
    LaneId outgoingLaneId = null;
    int offset = 0;
    LaneId tmpLaneId;
    do{
      Optional<LaneId> nextLaneIdOptional = car.getRouteOffsetLaneId(offset++);
      if(nextLaneIdOptional.isPresent()) {
        tmpLaneId = nextLaneIdOptional.get();
        LaneReadable tmpLane = tmpLaneId.getReadable(roadStructureReader);
        if (tmpLane != null && tmpLane.getIncomingJunctionId().equals(junctionId)) {
          outgoingLaneId = tmpLaneId;
        }
      }
      else{
        tmpLaneId = null;
      }
    }
    while(outgoingLaneId == null && tmpLaneId != null);
    return outgoingLaneId;
  }
}
