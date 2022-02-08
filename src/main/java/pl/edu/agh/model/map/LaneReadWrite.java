package pl.edu.agh.model.map;

import pl.edu.agh.model.car.Car;

public interface LaneReadWrite extends LaneReadOnly {
    // writable interface for Lane class + readable interface

    void addToIncomingCars(Car car) throws CarAlreadyAddedException;

    void clearIncomingCars();
}
