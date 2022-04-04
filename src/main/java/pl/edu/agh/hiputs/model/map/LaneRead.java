package pl.edu.agh.hiputs.model.map;

import pl.edu.agh.hiputs.model.car.CarRead;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.Optional;

public interface LaneRead {
    // readable interface for Lane class

    Optional<CarRead> getNextCarData(CarRead car);

    Optional<CarRead> getFirstCar();

    JunctionId getOutgoingJunction();

    double getLength();

    LaneId getId();
}
