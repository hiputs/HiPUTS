package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.Optional;
import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.partition.model.lights.control.SignalsControlCenter;

public interface JunctionReadable {

  JunctionId getJunctionId();

  Double getLongitude();

  Double getLatitude();

  Stream<RoadId> streamIncomingRoadIds();

  Stream<RoadId> streamOutgoingRoadIds();

  Stream<RoadOnJunction> streamRoadOnJunction();

  Optional<SignalsControlCenter> getSignalsControlCenter();
}
