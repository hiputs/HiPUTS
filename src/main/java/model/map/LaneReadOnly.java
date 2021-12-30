package model.map;

import model.car.CarReadOnly;

import java.util.Optional;

public interface LaneReadOnly {
    // readable interface for Lane class

    Optional<CarReadOnly> getNextCarData(CarReadOnly car);
}
