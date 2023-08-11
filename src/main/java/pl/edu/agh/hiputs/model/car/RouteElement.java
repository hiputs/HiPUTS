package pl.edu.agh.hiputs.model.car;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class RouteElement implements Cloneable{

  /**
   * JunctionId of junction that should be visited when following route.
   */
  private JunctionId junctionId;

  /**
   * Outgoing road of junction that should be visited when following route.
   */
  private RoadId outgoingRoadId;

  @Override
  public RouteElement clone() {
    try {
      final RouteElement clone = (RouteElement) super.clone();
      clone.setJunctionId(new JunctionId(junctionId.getValue(),junctionId.getJunctionType()));
      clone.setOutgoingRoadId(new RoadId(outgoingRoadId.getValue()));
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}
