package pl.edu.agh.hiputs.model.map;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.stream.Stream;

public interface PatchEditor extends PatchReader {

    LaneEditable getLaneEditable(LaneId laneId);

    Stream<LaneEditable> streamLanesEditable();

    JunctionEditable getJunctionEditable(JunctionId junctionId);

    Stream<JunctionEditable> streamJunctionsEditable();

}
