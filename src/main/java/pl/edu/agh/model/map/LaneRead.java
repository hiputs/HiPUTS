package pl.edu.agh.model.map;

import pl.edu.agh.model.car.CarRead;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

import java.util.Optional;

public interface LaneRead {
    // readable interface for Lane class

    Optional<CarRead> getNextCarData(CarRead car);

    Optional<CarRead> getFirstCar();

    JunctionId getOutgoingJunction();

    double getLength();

    LaneId getId();
}
