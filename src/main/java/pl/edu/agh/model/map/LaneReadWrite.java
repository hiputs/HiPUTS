package pl.edu.agh.model.map;

import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

public interface LaneReadWrite extends LaneReadOnly {
    // writable interface for Lane class + readable interface

    void addToIncomingCars(Car car) throws CarAlreadyAddedException;

    void clearIncomingCars();

    LaneId getId();

    JunctionId getOutgoingJunction();
}
