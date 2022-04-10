package pl.edu.agh.hiputs.model.map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Builder
@AllArgsConstructor
public class Lane implements LaneEditable {

    /**
     * Unique lane identifier.
     */
    @Getter
    private final LaneId id;

    /**
     * Collection of cars traveling on this lane.
     */
    @Builder.Default
    private final Deque<CarEditable> cars = new LinkedList<>();

    /**
     * Reference to lane that goes in opposite direction and is closest to this one.
     */
    @Getter
    @Builder.Default
    private final Optional<LaneId> oppositeLane = Optional.empty();

    /**
     * Reference to junction id that is at the begging of lane
     * j --------->
     */
    @Getter
    private final JunctionId incomingJunction;

    /**
     * Reference to junction id that is at the end of lane
     * ---------> j
     */
    @Getter
    private final JunctionId outgoingJunction;

    /**
     * Sign at the end of lane
     */
    @Getter
    private final Sign outSign;

    /**
     * Length of lane in meters
     */
    @Getter
    private final double length;

    /**
     * Set for cars incoming onto this lane
     */
    @Builder.Default
    private Set<CarEditable> incomingCars = new ConcurrentSkipListSet<>();

    @Override
    public Optional<CarReadable> getCarInFrontReadable(CarReadable car) {
        return cars.stream()
                .filter(otherCar -> otherCar.getPositionOnLane() > car.getPositionOnLane())
                .findFirst()
                .map(nextCar -> nextCar);
    }

    public Optional<CarReadable> getCarAtEntryReadable() {
        return Optional.ofNullable(cars.peekFirst());
    }

    @Override
    public Stream<CarReadable> streamCarsFromExitReadable() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(cars.descendingIterator(), 0), false
        );
    }

    @Override
    public void addIncomingCar(CarEditable car) {
        incomingCars.add(car);
        //TODO validate if cars is added properly to set (look before this commit version)
    }

    @Override
    public void addCarAtEntry(CarEditable car) {
        cars.addFirst(car);
    }

    @Override
    public Optional<CarEditable> pollCarAtExit() {
        return Optional.ofNullable(cars.pollLast());
    }

    @Override
    public Optional<CarEditable> getCarAtExit() {
        return Optional.ofNullable(cars.peekLast());
    }

    @Override
    public Stream<CarEditable> pollIncomingCars() {
        Set<CarEditable> oldIncomingCars = incomingCars;
        this.incomingCars = new HashSet<>();
        return oldIncomingCars.stream();
    }

    @Override
    public Stream<CarEditable> streamCarsFromExitEditable() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(cars.descendingIterator(), 0), false
        );
    }
}

