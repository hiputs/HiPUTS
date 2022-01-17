package pl.edu.agh.model.car;

import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.agh.model.actor.RoadStructureProvider;
import pl.edu.agh.model.follow.IDecider;
import pl.edu.agh.model.id.CarId;

import java.util.Optional;

public class Car implements CarReadOnly {

    /**
     * Unique car identifier.
     */
    private CarId id;

    /**
     * Lane on which car is currently situated and its location on this lane.
     */
    private LaneLocation location;

    /**
     * Route that car will follow and its location on this route.
     */
    private RouteLocation routeLocation;

    /**
     * Current speed of car.
     */
    private double speed = 0;

    /**
     * Length of the car.
     */
    private double length = 5;

    /**
     * Maximum possible speed of the car.
     */
    private double maxSpeed = 20;

    /**
     * Current acceleration of car.
     */
    private double acceleration = 0;

    private Optional<Decision> decision;

    /**
     * Decider instance
     */
    @Autowired
    private IDecider decider;

    public Car() {
    }

    public Car(double length, double maxSpeed) {
        this.length = length;
        this.maxSpeed = maxSpeed;
    }

    public void decide(RoadStructureProvider roadStructureProvider) {
        // make local decision based on read only road structure (watch environment) and save it locally

        //First prepare CarEnvironment
        CarEnvironment environment = new CarEnvironment();

        //Second call Decider
        decider.makeDecision(environment);
    }

    public CarUpdateResult update() {
        // extract information from decision and apply those changes to car
        throw new UnsupportedOperationException("method not implemented!");
    }

    public double getPosition() {
        return this.location.getPositionOnLane();
    }

    public void setPosition(double position) {
        if (this.location == null)
            this.location = new LaneLocation();
        this.location.setPositionOnLane(position);
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public double getMaxSpeed() {
        return maxSpeed;
    }
}
