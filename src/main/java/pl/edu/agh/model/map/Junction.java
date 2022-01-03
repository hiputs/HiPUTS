package pl.edu.agh.model.map;

import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

import java.util.Set;


@Getter
@Setter
public class Junction {
    /**
     * Unique junction identifier.
     */
    private JunctionId id;

    /**
     * Lanes incoming into this junction
     * ------> j <------
     */
    private Set<LaneId> incomingLanes;

    /**
     * Lanes outgoing from this junction
     * <------ j ------>
     */
    private Set<LaneId> outgoingLanes;


}
