package pl.edu.agh.model.map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.car.CarReadOnly;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter
@Setter
@RequiredArgsConstructor
public class Lane implements LaneReadWrite {

    /**
     * Unique lane identifier.
     */
    private final LaneId id;

    /**
     * Collection of cars traveling on this lane.
     */
    private LinkedList<Car> carsQueue = new LinkedList<>();

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
     * Length of lane in meters
     */
    private double length;

    /**
     * Set for cars incoming onto this lane
     */
    private Set<Car> incomingCars = new ConcurrentSkipListSet<Car>();

    public Lane() {
        this(new LaneId());
    }

    public Optional<CarReadOnly> getNextCarData(CarReadOnly car) {
        double carPosition = car.getPosition();
        CarReadOnly found = null;
        for (CarReadOnly nextCar : carsQueue) {
            if (nextCar.getPosition() > carPosition) {
                found = nextCar;
                break;
            }
        }
        Optional<CarReadOnly> carData;
        if (found != null)
            carData = Optional.of(found);
        else
            carData = Optional.empty();

        return carData;
    }

    public Optional<CarReadOnly> getFirstCar() {
        Optional<CarReadOnly> firstCar;
        try {
            firstCar = Optional.of(this.carsQueue.getFirst());
        } catch (Exception e) {
            firstCar = Optional.empty();
        }
        return firstCar;
    }

    public void addCarToLane(Car car) {
        this.carsQueue.addFirst(car);
    }

    public void addToIncomingCars(Car car) throws CarAlreadyAddedException {
        if (this.incomingCars.add(car))
            throw new CarAlreadyAddedException();
    }

    public void clearIncomingCars() {
        this.incomingCars.clear();
    }


}

class CarAlreadyAddedException extends Exception {
}