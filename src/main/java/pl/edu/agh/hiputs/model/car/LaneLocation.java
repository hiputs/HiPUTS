package pl.edu.agh.hiputs.model.car;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.hiputs.model.id.LaneId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LaneLocation {
    /**
     * Lane on which car is currently situated.
     */
    private LaneId lane = null;
    /**
     * Position of car at its lane.
     */
    private double positionOnLane = 0;
}
