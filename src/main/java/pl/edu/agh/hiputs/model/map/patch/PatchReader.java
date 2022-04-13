package pl.edu.agh.hiputs.model.map.patch;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

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
