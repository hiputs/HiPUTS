package pl.edu.agh.hiputs.model.map;

import lombok.Getter;
import pl.edu.agh.hiputs.model.id.LaneId;

@Getter
public class LaneOnJunction implements ILaneOnJunction {

    /**
     * Index of lane on junction
     */
    private final int laneIndexOnJunction;

    /**
     * Global lane Id
     */
    private final LaneId laneId;

    public LaneOnJunction(int laneOrder, LaneId laneId) {
        this.laneIndexOnJunction = laneOrder;
        this.laneId = laneId;
    }
}
