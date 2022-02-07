package pl.edu.agh.model.map;

import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

/**
 * writable interface for Lane class + readable interface
 */
public interface LaneReadWrite extends LaneReadOnly {

    void addFirstCar(Car car);

    Car removeLastCar();

    void addToIncomingCars(Car car) throws CarAlreadyAddedException;

    void clearIncomingCars();

    LaneId getId();

    JunctionId getOutgoingJunction();
}
