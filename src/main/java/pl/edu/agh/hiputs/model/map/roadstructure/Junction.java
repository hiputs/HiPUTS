package pl.edu.agh.hiputs.model.map.roadstructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.partition.model.lights.control.SignalsControlCenter;

@AllArgsConstructor
public class Junction implements JunctionReadable, JunctionEditable {

  /**
   * Unique junction identifier.
   */
  @Getter
  private final JunctionId junctionId;

  /**
   * Longitude of this junction
   */
  @Getter
  private final Double longitude;

  /**
   * Latitude of this junction
   */
  @Getter
  private final Double latitude;

  /**
   * Roads incoming into this junction
   */
  private final Set<RoadId> incomingRoadIds;

  /**
   * Roads outgoing from this junction
   */
  private final Set<RoadId> outgoingRoadIds;

  /**
   * All roads on this junction in order of index
   */
  private final List<RoadOnJunction> roadOnJunctions;

  /**
   * Traffic lights manager of the representing junction
   */
  private final Optional<SignalsControlCenter> signalsControlCenter;

  public static JunctionBuilder builder() {
    return new JunctionBuilder();
  }

  @Override
  public Stream<RoadId> streamIncomingRoadIds() {
    return incomingRoadIds.stream();
  }

  @Override
  public Stream<RoadId> streamOutgoingRoadIds() {
    return outgoingRoadIds.stream();
  }

  @Override
  public Stream<RoadOnJunction> streamRoadOnJunction() {
    return roadOnJunctions.stream();
  }

  public static class JunctionBuilder {

    private JunctionId junctionId = JunctionId.randomCrossroad();

    private Double longitude;
    private Double latitude;
    private Optional<SignalsControlCenter> signalsControlCenter = Optional.empty();
    private final Set<RoadId> incomingRoadIds = new HashSet<>();
    private final Set<RoadId> outgoingRoadIds = new HashSet<>();
    private final List<RoadOnJunction> roadsOnJunction = new ArrayList<>();

    public JunctionBuilder junctionId(JunctionId junctionId) {
      this.junctionId = junctionId;
      return this;
    }

    public JunctionBuilder longitude(Double longitude) {
      this.longitude = longitude;
      return this;
    }

    public JunctionBuilder latitude(Double latitude) {
      this.latitude = latitude;
      return this;
    }

    public JunctionBuilder addIncomingRoadId(RoadId roadId, boolean isSubordinate) {
      incomingRoadIds.add(roadId);
      roadsOnJunction.add(new RoadOnJunction(roadId, roadsOnJunction.size(), RoadDirection.INCOMING,
          isSubordinate ? RoadSubordination.SUBORDINATE : RoadSubordination.NOT_SUBORDINATE, TrafficLightColor.GREEN));
      return this;
    }

    public JunctionBuilder addOutgoingRoadId(RoadId roadId) {
      outgoingRoadIds.add(roadId);
      roadsOnJunction.add(
          new RoadOnJunction(roadId, roadsOnJunction.size(), RoadDirection.OUTGOING, RoadSubordination.NONE,
              TrafficLightColor.GREEN));
      return this;
    }

    public JunctionBuilder signalsControlCenter(SignalsControlCenter signalsControlCenter) {
      this.signalsControlCenter = Optional.of(signalsControlCenter);
      return this;
    }

    public Junction build() {
      return new Junction(junctionId, longitude, latitude, incomingRoadIds, outgoingRoadIds, roadsOnJunction, signalsControlCenter);
    }
  }
}
