package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

public interface JunctionReadable {

  JunctionId getJunctionId();

  Stream<LaneId> streamIncomingLaneIds();

  Stream<LaneId> streamOutgoingLaneIds();

  Stream<LaneOnJunction> streamLanesOnJunction();
}
