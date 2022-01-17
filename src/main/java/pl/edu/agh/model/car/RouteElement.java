package pl.edu.agh.model.car;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

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
     * Outgoing lane of junction that should be visited when following route.
     */
    private LaneId outgoingLaneId;
}
