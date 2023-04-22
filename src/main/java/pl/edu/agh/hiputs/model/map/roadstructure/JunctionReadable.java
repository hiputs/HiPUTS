package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;

public interface JunctionReadable {

  JunctionId getJunctionId();

  Double getLongitude();

  Double getLatitude();

  Stream<RoadId> streamIncomingRoadIds();

  Stream<RoadId> streamOutgoingRoadIds();

  Stream<RoadOnJunction> streamRoadOnJunction();
}
