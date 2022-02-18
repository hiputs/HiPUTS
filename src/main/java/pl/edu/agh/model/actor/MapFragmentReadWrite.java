package pl.edu.agh.model.actor;

import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.JunctionReadWrite;
import pl.edu.agh.model.map.LaneReadWrite;

public interface MapFragmentReadWrite extends MapFragmentRead {

    void addCar(LaneId laneId, Car car);

    Car removeLastCarFromLane(LaneId laneId);

    LaneReadWrite getLaneReadWriteById(LaneId laneId);

    JunctionReadWrite getJunctionReadWriteById(JunctionId junctionId);

}
