package pl.edu.agh.hiputs.model.map;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.stream.Stream;

public interface JunctionReadable {

    JunctionId getId();

    Stream<LaneId> streamIncomingLaneIds();

    Stream<LaneId> streamOutgoingLaneIds();
}
