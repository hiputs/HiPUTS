package model.map;

import model.car.Car;
import model.car.CarData;

import java.util.Optional;

public interface LaneReadOnly {
    // readable interface for Lane class

    public Optional<CarData> getNextCarData(Car car);
}
