package model.map;

import model.car.Car;
import model.id.LaneId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

class LaneLocal implements LaneRW {

    /**
     * Unique lane identifier.
     */
    private LaneId id;

    /**
     * Collection of cars traveling on this lane.
     */
    private Collection<Car> carsQueue = new ArrayList<>();

    /**
     * Reference to lane that goes in opposite direction and is closest to this one.
     */
    private Optional<LaneRW> oppositeLaneRef = Optional.empty();

    /**
     * Reference to junction that is at the begging of lane
     * j --------->
     */
    private Junction incomingJunction;

    /**
     * Reference to junction that is at the end of lane
     * ---------> j
     */
    private Junction outgoingJunction;

    /**
     * Sign at the end of lane
     */
    private Sign outSign;

    /**
     * Light signal at the end of lane
     */
    private LightSignal outSignal;

}
