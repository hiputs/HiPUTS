package pl.edu.agh.hiputs.model.map.patch;

import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

public interface PatchEditor extends PatchReader {

  LaneEditable getLaneEditable(LaneId laneId);

  Stream<LaneEditable> streamLanesEditable();

  Stream<LaneEditable> parallelStreamLanesEditable();

  JunctionEditable getJunctionEditable(JunctionId junctionId);

  Stream<JunctionEditable> streamJunctionsEditable();

  LaneEditable getAnyLane();

}
