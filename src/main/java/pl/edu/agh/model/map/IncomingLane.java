package pl.edu.agh.model.map;

import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.model.id.LaneId;

@Getter
@Setter
public class IncomingLane extends LaneOnJunction {

    /**
     * light color on lane (green if no traffic lights)
     */
    private TrafficLightColor LightColor;

    /**
     * True if incoming lane is subordinated on junction
     */
    private boolean isSubordinated;

    public IncomingLane(int laneOrder, LaneId laneId, boolean isSubordinated) {
        super(laneOrder, laneId);
        this.isSubordinated = isSubordinated;
    }
}
