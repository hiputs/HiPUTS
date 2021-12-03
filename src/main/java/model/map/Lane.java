package model.map;

import model.car.Car;
import model.id.JunctionId;
import model.id.LaneId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

class Lane implements LaneReadWrite {

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
    private Optional<LaneId> oppositeLane = Optional.empty();

    /**
     * Reference to junction id that is at the begging of lane
     * j --------->
     */
    private JunctionId incomingJunction;

    /**
     * Reference to junction id that is at the end of lane
     * ---------> j
     */
    private JunctionId outgoingJunction;

    /**
     * Sign at the end of lane
     */
    private Sign outSign;

    /**
     * Light signal at the end of lane
     */
    private LightSignal outSignal;

}
