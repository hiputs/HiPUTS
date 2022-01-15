package pl.edu.agh.model.car;

import pl.edu.agh.model.id.LaneId;

class LaneLocation {
    /**
     * Lane on which car is currently situated.
     */
    private LaneId lane = null;
    /**
     * Position of car at its lane.
     */
    private double positionOnLane = 0;

    public double getPositionOnLane() {
        return positionOnLane;
    }

    public void setPositionOnLane(double positionOnLane) {
        this.positionOnLane = positionOnLane;
    }
}