package pl.edu.agh.model.map;

import lombok.Getter;
import pl.edu.agh.model.id.LaneId;

@Getter
public class LaneOnJunction implements ILaneOnJunction{
    private int laneOrder;
    private LaneId laneId;

    public LaneOnJunction(int laneOrder, LaneId laneId) {
        this.laneOrder = laneOrder;
        this.laneId = laneId;
    }
}
