package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorReadable;

@Slf4j
@Builder
@AllArgsConstructor
public class Road implements RoadEditable {

  /**
   * Unique road identifier.
   */
  @Getter
  private final RoadId roadId;

  /**
   * List of available lanes in a Road.
   * Road consist only of lanes going in one direction
   */
  @Getter
  @Builder.Default
  private final List<LaneId> lanes = new ArrayList<>();

  /**
   * Reference to road that is on the left side of this one, for now it should be in opposite direction.
   */
  @Getter
  @Builder.Default
  private final Optional<NeighborRoadInfo> leftNeighbor = Optional.empty();

  /**
   * Reference to junction id that is at the beginning of road
   * j --------->
   */
  @Getter
  private final JunctionId incomingJunctionId;

  /**
   * Reference to junction id that is at the end of road
   * ---------> j
   */
  @Getter
  private final JunctionId outgoingJunctionId;

  /**
   * Sign at the end of road
   */
  @Getter
  private final Sign outSign;

  /**
   * Length of road in meters
   */
  @Getter
  private final double length;

  /**
   * Traffic indicator which prohibits or allows entering incomingJunction
   */
  @Getter
  @Builder.Default
  private final Optional<TrafficIndicatorReadable> trafficIndicator = Optional.empty();


}

