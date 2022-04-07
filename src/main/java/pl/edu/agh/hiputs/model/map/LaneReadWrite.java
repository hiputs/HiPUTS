package pl.edu.agh.hiputs.model.map;

import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * writable interface for Lane class + readable interface
 */
public interface LaneReadWrite extends LaneRead {

    void addFirstCar(Car car);

    Optional<Car> removeLastCar();

    Optional<Car> getLastCar();

    void addToIncomingCars(Car car);

    Set<Car> getIncomingCars();

    void clearIncomingCars();

    List<Car> getCars();

    LaneId getId();

    JunctionId getOutgoingJunction();

    List<Car> getAllCars();
}
