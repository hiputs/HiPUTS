package pl.edu.agh.hiputs.model.map.patch;

import java.util.Set;
import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;

public interface PatchReader {

  PatchId getPatchId();

  Set<RoadId> getRoadIds();

  RoadReadable getRoadReadable(RoadId roadId);

  Stream<RoadReadable> streamRoadReadable();

  Set<LaneId> getLaneIds();

  LaneReadable getLaneReadable(LaneId laneId);

  Stream<LaneEditable> streamLanesEditable();

  Stream<LaneReadable> streamLanesReadable();

  Set<JunctionId> getJunctionIds();

  JunctionReadable getJunctionReadable(JunctionId junctionId);

  Stream<JunctionReadable> streamJunctionsReadable();

}
