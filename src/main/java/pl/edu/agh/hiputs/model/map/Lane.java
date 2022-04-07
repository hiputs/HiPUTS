package pl.edu.agh.hiputs.model.map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarRead;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.LinkedList;
import java.util.List;
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
    @Getter
    private Set<Car> incomingCars = new ConcurrentSkipListSet<>();

    public Lane() {
        this(new LaneId());
    }

    public Optional<CarRead> getNextCarData(CarRead car) {
        double carPosition = car.getPositionOnLane();
        CarRead found = null;
        for (CarRead nextCar : carsQueue) {
            if (nextCar.getPositionOnLane() > carPosition) {
                found = nextCar;
                break;
            }
        }
        Optional<CarRead> carData;
        if (found != null)
            carData = Optional.of(found);
        else
            carData = Optional.empty();

        return carData;
    }

    public Optional<CarRead> getFirstCar() {
        return carsQueue.isEmpty() ? Optional.empty() : Optional.of(carsQueue.getFirst());
    }

    public void addFirstCar(Car car) {
        carsQueue.addFirst(car);
    }

    public Optional<Car> removeLastCar() {
        return carsQueue.isEmpty() ? Optional.empty() : Optional.of(carsQueue.removeLast());
    }

    public Optional<Car> getLastCar() {
        return carsQueue.isEmpty() ? Optional.empty() : Optional.of(carsQueue.getLast());
    }

    public void addToIncomingCars(Car car) {
        incomingCars.add(car);
        //TODO validate if cars is added properly to set (look before this commit version)
    }

    public void clearIncomingCars() {
        this.incomingCars.clear();
    }

    @Override
    public List<Car> getCars() {
        return getCarsQueue();
    }

    @Override
    public List<Car> getAllCars() {
        return carsQueue;
    }

}

