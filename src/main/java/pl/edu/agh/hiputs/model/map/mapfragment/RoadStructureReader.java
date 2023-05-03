package pl.edu.agh.hiputs.model.map.mapfragment;

import java.util.List;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

public interface RoadStructureReader {

  RoadReadable getRoadReadable(RoadId roadId);

  RoadReadable getRoadReadableFromLaneId(LaneId laneId);

  LaneReadable getLaneReadable(LaneId laneId);

  List<LaneReadable> getLaneSuccessorsReadable(LaneId laneId);

  List<LaneReadable> getRoadToLaneSuccessorsReadable(RoadId roadId);

  JunctionReadable getJunctionReadable(JunctionId junctionId);

}
