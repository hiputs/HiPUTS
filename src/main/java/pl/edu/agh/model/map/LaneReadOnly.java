package pl.edu.agh.model.map;

import pl.edu.agh.model.car.CarReadOnly;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

import java.util.Optional;

public interface LaneReadOnly {
    // readable interface for Lane class

    Optional<CarReadOnly> getNextCarData(CarReadOnly car);

    Optional<CarReadOnly> getFirstCar();

    JunctionId getOutgoingJunction();

    double getLength();

    LaneId getId();
}
