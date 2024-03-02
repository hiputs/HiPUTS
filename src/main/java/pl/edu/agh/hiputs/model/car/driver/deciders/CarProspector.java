package pl.edu.agh.hiputs.model.car.driver.deciders;

import java.util.List;
import java.util.Optional;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CarBasicDeciderData;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CarTrailDeciderData;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadOnJunction;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

public interface CarProspector {

  CarFollowingEnvironment getFollowingCar(CarReadable currentCar, RoadStructureReader roadStructureReader);

  CarPrecedingEnvironment getPrecedingCar(CarReadable currentCar, RoadStructureReader roadStructureReader);

  CarPrecedingEnvironment getPrecedingCarOrCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader);

  CarPrecedingEnvironment getPrecedingCrossroad(CarReadable currentCar, RoadStructureReader roadStructureReader);

  List<RoadId> getConflictRoadIds(List<RoadOnJunction> roadOnJunctions, RoadId incomingRoadId, RoadId outgoingRoadId,
      CarId currentCarId, RoadStructureReader roadStructureReader);

  List<CarBasicDeciderData> getFirstCarsFromRoads(List<RoadId> conflictRoads, RoadStructureReader roadStructureReader);

  List<CarTrailDeciderData> getAllConflictCarsFromRoads(List<RoadId> conflictRoads, RoadStructureReader roadStructureReader, double conflictAreaLength);

  RoadId getNextOutgoingRoad(CarReadable car, JunctionId junctionId, RoadStructureReader roadStructureReader);

  List<RoadOnJunction> getRightRoadsOnJunction(List<RoadOnJunction> roadOnJunctions, RoadId incomingRoadId, RoadId outgoingRoadId);

  List<CarReadable> getAllFirstCarsFromRoads(List<RoadId> roads, RoadStructureReader roadStructureReader);

  List<LaneReadable> getNextLanes(LaneReadable currentLane, RoadId nextRoadId, RoadStructureReader roadStructureReader);

  List<LaneReadable> getCorrectIncomingLanes(RoadReadable road, RoadId nextRoadId,
      RoadStructureReader roadStructureReader);

  Optional<LaneId> getNarrowingRoadLaneSuccessor(RoadReadable currentRoad, LaneId currentLaneId, RoadReadable nextRoad);

  double getViewRange();
}
