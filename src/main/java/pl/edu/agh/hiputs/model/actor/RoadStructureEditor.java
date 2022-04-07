package pl.edu.agh.hiputs.model.actor;

import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.JunctionReadWrite;
import pl.edu.agh.hiputs.model.map.LaneReadWrite;

import java.util.Optional;

public interface RoadStructureEditor extends RoadStructureReader {

    void addCar(LaneId laneId, Car car);

    Optional<Car> removeLastCarFromLane(LaneId laneId);

    LaneReadWrite getLaneReadWriteById(LaneId laneId);

    JunctionReadWrite getJunctionReadWriteById(JunctionId junctionId);

}
