package model.car;

import model.id.LaneId;

class LaneLocation {
    /**
     * Lane on which car is currently situated.
     */
    private LaneId lane = null;
    /**
     * Position of car at its lane.
     */
    private double positionOnLane = 0;
}
