package pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarPrecedingEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarProspector;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

@RequiredArgsConstructor
@Slf4j
public class LaneChangeDecider implements ILaneChangeDecider {
  private final CarProspector prospector;
  private final ILaneChangeChecker mobilModel;
  private static double politenessFactor = 1.0;

  @Override
  public LaneChangeDecision makeDecision(CarReadable managedCar, CarPrecedingEnvironment nextCrossroad,
      RoadStructureReader roadStructureReader) {
    RoadReadable currentRoad = roadStructureReader.getRoadReadable(managedCar.getRoadId());

    if (currentRoad.getLanes().size() == 1) {
      //TODO
      return new LaneChangeDecision(0, managedCar.getLaneId());
    }

    // check for mandatory Lane Changes
    if (nextCrossroad.getNextCrossroadId().isPresent()) {
      LaneReadable incomingLane = roadStructureReader.getLaneReadable(nextCrossroad.getIncomingLaneId().get());
      RoadId incomingRoadId = nextCrossroad.getIncomingRoadId().get();
      RoadReadable incomingRoad = roadStructureReader.getRoadReadable(incomingRoadId);

      Optional<RoadId> outgoingRoadId = targetRoadAfterCrossroad(managedCar,currentRoad,incomingRoadId, roadStructureReader);
      //change lane before crossroad due to wrong lane before
      if (outgoingRoadId.isPresent()) {
        List<LaneReadable> outgoingLanes = prospector.getNextLanes(incomingLane, outgoingRoadId.get(), roadStructureReader);

        if (outgoingLanes.isEmpty()) {
          return makeMandatoryLaneChangeDecision(managedCar, nextCrossroad, incomingRoad, incomingLane, outgoingRoadId.get(), roadStructureReader);
        }
      }
    }


    return getLaneChangeDecision(managedCar, politenessFactor, roadStructureReader,Set.of(LaneChange.LEFT, LaneChange.RIGHT));
  }

  private LaneChangeDecision makeMandatoryLaneChangeDecision(CarReadable managedCar, CarPrecedingEnvironment nextCrossroad,
      RoadReadable incomingRoad, LaneReadable incomingLane, RoadId outgoingRoadId, RoadStructureReader roadStructureReader) {

    long incomingLaneIndex = incomingRoad.getLanes().indexOf(incomingLane.getLaneId());
    List<LaneReadable> correctIncomingLanes = prospector.getCorrectIncomingLanes(incomingRoad,outgoingRoadId,roadStructureReader);

    Set<LaneChange> laneChanges = correctIncomingLanes
        .stream()
        .map(lane -> incomingRoad.getLanes().indexOf(lane.getLaneId()) < incomingLaneIndex ? LaneChange.LEFT : LaneChange.RIGHT)
        .collect(Collectors.toSet());

    if (laneChanges.contains(LaneChange.LEFT) && laneChanges.contains(LaneChange.RIGHT)) {
      log.error("Incorrect Lane Change Decision error");
    }
    //  change lane - left or right check
    double distanceToCrossroad = nextCrossroad.getDistance();
    double politenessFactor = distanceToCrossroad/prospector.getViewRange();

    return getLaneChangeDecision(managedCar, politenessFactor, roadStructureReader, laneChanges);
  }

  private LaneChangeDecision getLaneChangeDecision(CarReadable managedCar, double politenessFactor,
      RoadStructureReader roadStructureReader, Set<LaneChange> laneChanges) {

    RoadReadable currentRoad = roadStructureReader.getRoadReadable(managedCar.getRoadId());

    //check left lane
    int currentLaneIndex = currentRoad.getLanes().indexOf(managedCar.getLaneId());

    if (laneChanges.contains(LaneChange.LEFT) && currentLaneIndex > 0) {
      LaneId targetLaneId = currentRoad.getLanes().get(currentLaneIndex - 1);
      MobilModelDecision mobilModelDecision = this.mobilModel.makeDecision(managedCar,targetLaneId,politenessFactor,
          roadStructureReader);
      //TODO
      return mobilModelDecision.isCanChangeLane() ? new LaneChangeDecision(mobilModelDecision.getAcceleration(),
          targetLaneId) : new LaneChangeDecision(0, managedCar.getLaneId());
    }

    if (laneChanges.contains(LaneChange.RIGHT) && currentLaneIndex < currentRoad.getLanes().size()-1) {
      LaneId targetLaneId = currentRoad.getLanes().get(currentLaneIndex + 1);
      MobilModelDecision mobilModelDecision = this.mobilModel.makeDecision(managedCar,targetLaneId,politenessFactor,
          roadStructureReader);
      //TODO
      return mobilModelDecision.isCanChangeLane() ? new LaneChangeDecision(mobilModelDecision.getAcceleration(),
          targetLaneId) : new LaneChangeDecision(0, managedCar.getLaneId());
    }
    return new LaneChangeDecision(0, managedCar.getLaneId());
  }

  private Optional<RoadId> targetRoadAfterCrossroad(CarReadable car, RoadReadable currentRoad, RoadId incomingRoadId, RoadStructureReader roadStructureReader) {
    Optional<RoadId> road = Optional.of(currentRoad.getRoadId());
    int offset = 0;

    while (road.isPresent() && !incomingRoadId.equals(road.get())) {
      road = car.getRouteOffsetRoadId(++offset);
    }

    if (road.isPresent()) {
      road = car.getRouteOffsetRoadId(++offset);
    }
    return road;
  }

}
