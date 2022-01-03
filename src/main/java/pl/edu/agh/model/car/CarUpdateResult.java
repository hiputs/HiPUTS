package pl.edu.agh.model.car;

import lombok.Data;
import pl.edu.agh.model.id.LaneId;

@Data
public class CarUpdateResult {

    private LaneId oldLaneId;

    private LaneLocation newLaneLocation;

}
