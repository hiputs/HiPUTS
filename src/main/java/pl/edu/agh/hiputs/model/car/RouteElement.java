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
public class RouteElement {

  /**
   * JunctionId of junction that should be visited when following route.
   */
  private JunctionId junctionId;

  /**
   * Outgoing road of junction that should be visited when following route.
   */
  private RoadId outgoingRoadId;
}
