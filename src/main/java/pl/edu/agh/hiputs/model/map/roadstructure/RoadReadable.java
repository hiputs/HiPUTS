package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;

// readable interface for Road class
public interface RoadReadable {

  RoadId getRoadId();

  double getLength();

  List<LaneId> getLanes();

  JunctionId getIncomingJunctionId();

  JunctionId getOutgoingJunctionId();

  Optional<NeighborRoadInfo> getLeftNeighbor();
}
