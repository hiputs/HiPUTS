package pl.edu.agh.hiputs.model.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
    private final JunctionId junctionId;

    /**
     * Lanes incoming into this junction
     */
    private final Set<LaneId> incomingLaneIds;

    /**
     * Lanes outgoing from this junction
     */
    private final Set<LaneId> outgoingLaneIds;


    /**
     * All lanes on this junction in order of index
     */
    private final List<LaneOnJunction> lanesOnJunction;

    public static JunctionBuilder builder() {
        return new JunctionBuilder();
    }

    @Override
    public Stream<LaneId> streamIncomingLaneIds() {
        return incomingLaneIds.stream();
    }

    @Override
    public Stream<LaneId> streamOutgoingLaneIds() {
        return outgoingLaneIds.stream();
    }

    public static class JunctionBuilder {
        private JunctionId junctionId = JunctionId.randomCrossroad();
        private final Set<LaneId> incomingLaneIds = new HashSet<>();
        private final Set<LaneId> outgoingLaneIds = new HashSet<>();
        private final List<LaneOnJunction> lanesOnJunction = new ArrayList<>();

        public JunctionBuilder junctionId(JunctionId junctionId) {
            this.junctionId = junctionId;
            return this;
        }

        public JunctionBuilder addIncomingLaneId(LaneId laneId, boolean isSubordinate) {
            incomingLaneIds.add(laneId);
            lanesOnJunction.add(new LaneOnJunction(
                    laneId,
                    lanesOnJunction.size(),
                    LaneDirection.INCOMING,
                    isSubordinate ? LaneSubordination.SUBORDINATE : LaneSubordination.NOT_SUBORDINATE,
                    TrafficLightColor.GREEN
            ));
            return this;
        }

        public JunctionBuilder addOutgoingLaneId(LaneId laneId) {
            outgoingLaneIds.add(laneId);
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
            return new Junction(junctionId, incomingLaneIds, outgoingLaneIds, lanesOnJunction);
        }
    }
}
