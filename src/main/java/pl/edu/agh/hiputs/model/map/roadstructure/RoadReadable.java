package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorReadable;

// readable interface for Road class
public interface RoadReadable {

  RoadId getRoadId();

  double getLength();

  List<LaneId> getLanes();

  JunctionId getIncomingJunctionId();

  JunctionId getOutgoingJunctionId();

  Optional<NeighborRoadInfo> getLeftNeighbor();

  /**
   * Returns a traffic indicator which prohibits or allows entering incomingJunction
   */
  Optional<TrafficIndicatorReadable> getTrafficIndicator();
}
