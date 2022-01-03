package pl.edu.agh.model.map;

import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.car.CarReadOnly;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

import java.util.LinkedList;
import java.util.Optional;

public class Lane implements LaneReadWrite {

    /**
     * Unique lane identifier.
     */
    private LaneId id;

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
     * Light signal at the end of lane
     */
    private LightSignal outSignal;

    public Optional<CarReadOnly> getNextCarData(CarReadOnly car){
        double carPosition = car.getPosition();
        CarReadOnly found = null;
        for(CarReadOnly nextCar : carsQueue){
            if(nextCar.getPosition() > carPosition){
                found = nextCar;
                break;
            }
        }
        Optional<CarReadOnly> carData;
        if(found != null)
            carData = Optional.of(found);
        else
            carData = Optional.empty();

        return carData;
    }

    public void addCarToLane(Car car) {
        this.carsQueue.addFirst(car);
    }
}
