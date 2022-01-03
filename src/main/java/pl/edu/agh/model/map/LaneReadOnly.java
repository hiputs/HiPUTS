package pl.edu.agh.model.map;

import pl.edu.agh.model.car.CarReadOnly;

import java.util.Optional;

public interface LaneReadOnly {
    // readable interface for Lane class

    Optional<CarReadOnly> getNextCarData(CarReadOnly car);
}
