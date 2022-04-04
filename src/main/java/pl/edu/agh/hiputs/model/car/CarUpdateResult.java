package pl.edu.agh.hiputs.model.car;

import lombok.Data;
import pl.edu.agh.hiputs.model.id.LaneId;

@Data
public class CarUpdateResult {

    private LaneId oldLaneId;

    private LaneLocation newLaneLocation;

}
