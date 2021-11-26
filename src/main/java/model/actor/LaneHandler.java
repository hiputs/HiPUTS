package model.actor;

import model.car.Car;
import model.id.LaneId;
import model.map.LaneR;

public interface LaneHandler {

    LaneR getRLane(LaneId laneId);

    void stage(Car car);
}
