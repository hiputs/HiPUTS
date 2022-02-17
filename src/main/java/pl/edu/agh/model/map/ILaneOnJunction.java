package pl.edu.agh.model.map;

import pl.edu.agh.model.id.LaneId;

public interface ILaneOnJunction {
    /**
     * Order of line on junction
     * <------ j ------>
     */
    int getLaneOrder();

    /**
     * Global line Id
     * <------ j ------>
     */
    LaneId getLaneId();
}
