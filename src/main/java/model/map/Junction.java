package model.map;

import lombok.Getter;
import lombok.Setter;
import model.id.JunctionId;
import model.id.LaneId;

import java.util.Map;


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
    private Map<LaneId, LaneLocal> incomingLanes;

    /**
     * Lanes outgoing from this junction
     * <------ j ------>
     */
    private Map<LaneId, LaneReadWrite> outgoingLanes;


}
