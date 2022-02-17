package pl.edu.agh.model.car;

import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.model.id.LaneId;

@Getter
@Setter
class LaneLocation {
    /**
     * Lane on which car is currently situated.
     */
    private LaneId lane = null;
    /**
     * Position of car at its lane.
     */
    private double positionOnLane = 0;
}
