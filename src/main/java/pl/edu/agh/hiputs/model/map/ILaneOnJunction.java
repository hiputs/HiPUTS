package pl.edu.agh.hiputs.model.map;

import pl.edu.agh.hiputs.model.id.LaneId;

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
