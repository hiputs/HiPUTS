package pl.edu.agh.hiputs.model.car;

import java.util.List;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneOnJunction;

public interface CarProspector {

  CarEnvironment getPrecedingCarOrCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader);

  List<LaneId> getConflictLaneIds(List<LaneOnJunction> lanesOnJunction, LaneId incomingLaneId, LaneId outgoingLaneId);

  List<CarBasicDeciderData> getConflictCars(List<LaneId> conflictLanes, RoadStructureReader roadStructureReader);

  LaneId getNextOutgoingLane(CarReadable car, JunctionId junctionId, RoadStructureReader roadStructureReader);
}
