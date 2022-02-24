package pl.edu.agh.model.map;

import pl.edu.agh.model.id.LaneId;

public interface ILaneOnJunction {
    /**
     * Order of line on junction
     */
    int getLaneOrder();

    /**
     * Global line Id
     */
    LaneId getLaneId();
}
