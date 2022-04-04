package pl.edu.agh.hiputs.model.actor;

import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.JunctionReadWrite;
import pl.edu.agh.hiputs.model.map.LaneReadWrite;

public interface MapFragmentReadWrite extends MapFragmentRead {

    void addCar(LaneId laneId, Car car);

    Car removeLastCarFromLane(LaneId laneId);

    LaneReadWrite getLaneReadWriteById(LaneId laneId);

    JunctionReadWrite getJunctionReadWriteById(JunctionId junctionId);

}
