package pl.edu.agh.hiputs.model.map;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.Set;
import java.util.stream.Stream;

public interface PatchReader {

    Set<LaneId> getLaneIds();

    LaneReadable getLaneReadable(LaneId laneId);

    Stream<LaneReadable> streamLanesReadable();

    Set<JunctionId> getJunctionIds();

    JunctionReadable getJunctionReadable(JunctionId junctionId);

    Stream<JunctionReadable> streamJunctionsReadable();
}
