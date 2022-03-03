package pl.edu.agh.model.map;

import pl.edu.agh.model.id.LaneId;

public interface ILaneOnJunction {
    /**
     * Order of lane on junction
     */
    int getLaneOrder();

    /**
     * Global lane Id
     */
    LaneId getLaneId();
}
