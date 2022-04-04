package pl.edu.agh.hiputs.model.car;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.edu.agh.hiputs.model.id.LaneId;

@Data
@AllArgsConstructor
public class CarUpdateResult {

    private final LaneId oldLaneId;
    private final LaneId newLaneId;
    private final double newPositionOnLane;

}
