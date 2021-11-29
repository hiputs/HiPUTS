package model.actor;

import model.car.Car;
import model.id.LaneId;
import model.map.LaneReadWrite;

public interface LaneEditor {

    LaneReadWrite getLaneReadWrite(LaneId laneId);

    void stage(Car car);
}
