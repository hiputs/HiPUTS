package pl.edu.agh.hiputs.model.map;

import lombok.*;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


@AllArgsConstructor
public class Junction implements JunctionReadable, JunctionEditable {
    /**
     * Unique junction identifier.
     */
    @Getter
    private final JunctionId id;

    /**
     * Lanes incoming into this junction
     */
    private final Set<LaneId> incomingLanes;

    /**
     * Lanes outgoing from this junction
     */
    private final Set<LaneId> outgoingLanes;
    
    
    /**
     * All lanes on this junction in order of index
     */
    private final List<LaneOnJunction> lanesOnJunction;
    
    public static JunctionBuilder builder() {
        return new JunctionBuilder();
    }
    
    @Override
    public Stream<LaneId> streamIncomingLaneIds() {
        return incomingLanes.stream();
    }
    
    @Override
    public Stream<LaneId> streamOutgoingLaneIds() {
        return outgoingLanes.stream();
    }
    
    public static class JunctionBuilder {
        private JunctionId id = JunctionId.randomCrossroad();
        private Set<LaneId> incomingLanes = new HashSet<>();
        private Set<LaneId> outgoingLanes = new HashSet<>();
        private List<LaneOnJunction> lanesOnJunction = new ArrayList<>();
    
        public JunctionBuilder id(JunctionId id) {
            this.id = id;
            return this;
        }
    
        public JunctionBuilder addIncomingLane(LaneId laneId, boolean isSubordinate) {
            incomingLanes.add(laneId);
            lanesOnJunction.add(new LaneOnJunction(
                    laneId,
                    lanesOnJunction.size(),
                    LaneDirection.INCOMING,
                    isSubordinate ? LaneSubordination.SUBORDINATE : LaneSubordination.NOT_SUBORDINATE,
                    TrafficLightColor.GREEN
            ));
            return this;
        }
    
        public JunctionBuilder addOutgoingLane(LaneId laneId) {
            outgoingLanes.add(laneId);
            lanesOnJunction.add(new LaneOnJunction(
                    laneId,
                    lanesOnJunction.size(),
                    LaneDirection.OUTGOING,
                    LaneSubordination.NONE,
                    TrafficLightColor.GREEN
            ));
            return this;
        }
        
        public Junction build() {
            return new Junction(id, incomingLanes, outgoingLanes, lanesOnJunction);
        }
    }
}
