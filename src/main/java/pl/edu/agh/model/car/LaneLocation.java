package pl.edu.agh.model.car;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.model.id.LaneId;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
