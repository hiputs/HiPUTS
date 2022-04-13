package pl.edu.agh.hiputs.model.map.patch;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;

import java.util.stream.Stream;

public interface PatchEditor extends PatchReader {

    LaneEditable getLaneEditable(LaneId laneId);

    Stream<LaneEditable> streamLanesEditable();

    JunctionEditable getJunctionEditable(JunctionId junctionId);

    Stream<JunctionEditable> streamJunctionsEditable();

}
