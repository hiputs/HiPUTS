package pl.edu.agh.model.map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

import java.util.HashSet;
import java.util.Set;


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
    private Set<LaneId> incomingLanes = new HashSet<>();

    /**
     * Lanes outgoing from this junction
     * <------ j ------>
     */
    private Set<LaneId> outgoingLanes = new HashSet<>();

    public Junction() {
        this(new JunctionId());
    }
}
