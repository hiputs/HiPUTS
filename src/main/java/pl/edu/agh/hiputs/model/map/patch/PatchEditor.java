package pl.edu.agh.hiputs.model.map.patch;

import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;

public interface PatchEditor extends PatchReader {

  RoadEditable getRoadEditable(RoadId roadId);

  LaneEditable getLaneEditable(LaneId laneId);

  Stream<RoadEditable> streamRoadsEditable();

  Stream<RoadEditable> parallelStreamRoadsEditable();

  Stream<LaneEditable> parallelStreamLanesEditable();

  JunctionEditable getJunctionEditable(JunctionId junctionId);

  Stream<JunctionEditable> streamJunctionsEditable();

  RoadEditable getAnyRoad();

}
