package pl.edu.agh.hiputs.model.map;

import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.Optional;
import java.util.stream.Stream;

// readable interface for Lane class
public interface LaneReadable {

    LaneId getId();

    double getLength();

    JunctionId getIncomingJunction();

    JunctionId getOutgoingJunction();

    /**
     * Returns the nearest car between the given one and the outgoing junction (i.e. in front of the given one).
     */
    Optional<CarReadable> getCarInFrontReadable(CarReadable car);

    /**
     * Returns the car closest to the incoming junction.
     */
    Optional<CarReadable> getCarAtEntryReadable();

    /**
     * Returns a stream of cars, beginning from the one closest to the outgoing junction.
     */
    Stream<CarReadable> streamCarsFromExitReadable();
}
