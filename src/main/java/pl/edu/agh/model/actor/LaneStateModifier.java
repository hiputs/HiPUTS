package pl.edu.agh.model.actor;

import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.id.LaneId;

public interface LaneStateModifier {

    void addCar(LaneId laneId, Car car);

    Car removeLastCarFromLane(LaneId laneId);

}
