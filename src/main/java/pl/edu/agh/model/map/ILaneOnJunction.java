package pl.edu.agh.model.map;

import pl.edu.agh.model.id.LaneId;

public interface ILaneOnJunction {
    /**
     * Index of lane on junction
     */
    int getLaneIndexOnJunction();

    /**
     * Global lane Id
     */
    LaneId getLaneId();
}
