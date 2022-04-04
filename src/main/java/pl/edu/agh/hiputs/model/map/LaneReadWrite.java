package pl.edu.agh.hiputs.model.map;

import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.List;
import java.util.Set;

/**
 * writable interface for Lane class + readable interface
 */
public interface LaneReadWrite extends LaneRead {

    void addFirstCar(Car car);

    Car removeLastCar();

    void addToIncomingCars(Car car) throws CarAlreadyAddedException;

    Set<Car> getIncomingCars();

    void clearIncomingCars();

    List<Car> getCars();

    LaneId getId();

    JunctionId getOutgoingJunction();

    List<Car> getAllCars();
}
