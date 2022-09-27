package pl.edu.agh.hiputs.model.car.driver.deciders;

import java.util.List;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CarBasicDeciderData;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CarTrailDeciderData;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneOnJunction;

public interface CarProspector {
  CarEnvironment getPrecedingCar(CarReadable currentCar, RoadStructureReader roadStructureReader);

  CarEnvironment getPrecedingCarOrCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader);

  List<LaneId> getConflictLaneIds(List<LaneOnJunction> lanesOnJunction, LaneId incomingLaneId, LaneId outgoingLaneId,
      CarId currentCarId, RoadStructureReader roadStructureReader);

  List<CarBasicDeciderData> getFirstCarsFromLanes(List<LaneId> conflictLanes, RoadStructureReader roadStructureReader);

  List<CarTrailDeciderData> getAllConflictCarsFromLanes(List<LaneId> conflictLanes, RoadStructureReader roadStructureReader, double conflictAreaLength);

  LaneId getNextOutgoingLane(CarReadable car, JunctionId junctionId, RoadStructureReader roadStructureReader);

  List<LaneOnJunction> getRightLanesOnJunction(List<LaneOnJunction> lanesOnJunction, LaneId incomingLaneId, LaneId outgoingLaneId);

  List<CarReadable> getAllFirstCarsFromLanesReadable(List<LaneId> lanes, RoadStructureReader roadStructureReader);
}
