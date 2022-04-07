package pl.edu.agh.hiputs.model.map;

import lombok.*;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Builder
@AllArgsConstructor
public class Junction implements JunctionReadable, JunctionEditable {
    /**
     * Unique junction identifier.
     */
    @Builder.Default
    private final JunctionId id = JunctionId.randomCrossroad();

    /**
     * Lanes incoming into this junction
     */
    @Builder.Default
    private ArrayList<IncomingLane> incomingLanes = new ArrayList<>();

    /**
     * Lanes outgoing from this junction
     */
    @Builder.Default
    private ArrayList<ILaneOnJunction> outgoingLanes = new ArrayList<>();

    /**
     * Amount of all lanes on this junction
     */
    @Builder.Default
    private int lanesCount = 0;

    public void addIncomingLane(LaneId laneId, boolean isSubordinated) {
        incomingLanes.add(new IncomingLane(lanesCount++, laneId, isSubordinated));
    }

    public void addOutgoingLane(LaneId laneId) {
        outgoingLanes.add(new LaneOnJunction(lanesCount++, laneId));
    }

    @Override
    public Set<LaneId> getOutgoingLaneIds() {
        return outgoingLanes.stream()
                .map(ILaneOnJunction::getLaneId)
                .collect(Collectors.toSet());
    }
}
