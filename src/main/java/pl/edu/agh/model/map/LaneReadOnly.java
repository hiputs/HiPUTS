package pl.edu.agh.model.map;

import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.car.CarReadOnly;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LaneReadOnly {
    // readable interface for Lane class

    Optional<CarReadOnly> getNextCarData(CarReadOnly car);

    Optional<CarReadOnly> getFirstCar();

    JunctionId getOutgoingJunction();

    double getLength();

    LaneId getId();
}
