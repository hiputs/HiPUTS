package pl.edu.agh.model.map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

import java.util.ArrayList;


@Getter
@Setter
@RequiredArgsConstructor
public class Junction {
    /**
     * Unique junction identifier.
     */
    private final JunctionId id;

    /**
     * Lanes incoming into this junction
     * ------> j <------
     */
    private ArrayList<IncomingLane> incomingLanes = new ArrayList<>();

    /**
     * Lanes outgoing from this junction
     * <------ j ------>
     */
    private ArrayList<ILaneOnJunction> outgoingLanes = new ArrayList<>();

    /**
     * Amount of all lanes on this junction
     * <------ j ------>
     */
    private int lanesCount = 0;

    public Junction() {
        this(new JunctionId());
    }


    public void addIncomingLane(LaneId laneId, boolean isSubordinated) {
        incomingLanes.add(new IncomingLane(lanesCount++, laneId, isSubordinated));
    }

    public void addOutgoingLane(LaneId laneId) {
        outgoingLanes.add(new LaneOnJunction(lanesCount++, laneId));
    }
}
