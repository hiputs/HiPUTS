package model.car;

import lombok.Data;
import model.id.LaneId;

@Data
public class CarUpdateResult {

    private LaneId oldLaneId;

    private LaneLocation newLaneLocation;

}
