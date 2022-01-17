package pl.edu.agh.model.car;

import org.springframework.beans.factory.annotation.Configurable;
import pl.edu.agh.model.actor.ActorContext;
import pl.edu.agh.model.actor.RoadStructureProvider;
import pl.edu.agh.model.follow.IDMecider;
import pl.edu.agh.model.id.CarId;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.Junction;
import pl.edu.agh.model.map.Lane;
import pl.edu.agh.model.map.LaneReadOnly;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

@Configurable
public class Car implements CarReadOnly, Comparable<Car> {

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

    /**
     * Decision on how car state should be changed. Calculated by decider.
     */
    private Optional<Decision> decision;

    /**
     * Decider instance
     */
    @Inject
    private IDMecider decider;

    /**
     * ActorContext instance
     */
    private ActorContext actorContext; // TODO provide actorContext

    public Car(){
    }

    public Car(double length, double maxSpeed) {
        this.length = length;
        this.maxSpeed = maxSpeed;
    }

    public void decide(RoadStructureProvider roadStructureProvider) {
        // make local decision based on read only road structure (watch environment) and save it locally

        //First prepare CarEnvironment
        CarEnvironment environment = this.getPrecedingCar();

        //Second call Decider
        decider.makeDecision(this, environment);
    }

    public CarUpdateResult update() {
        // extract information from decision and apply those changes to car
        throw new UnsupportedOperationException("method not implemented!");
    }

    /**
     * Search for preceding car on the way counting distance to car, or distance to crossroad
     * @return precedingCar and distance
     */
    private CarEnvironment getPrecedingCar () {
        LaneReadOnly lane = this.actorContext.getLane(this.location.getLane());
        JunctionId nextJunctionId = lane.getOutgoingJunction();
        Optional<CarReadOnly> precedingCar = lane.getNextCarData(this);
        double distance;
        if(nextJunctionId.isCrossroad() || precedingCar.isPresent())
            distance = precedingCar.map(car -> car.getPosition() - car.getLength()).orElse(lane.getLength()) - this.getPosition();
        else {
            distance = lane.getLength() - this.getPosition();
            LaneId nextLaneId = route.getNextLaneId();
            lane = this.actorContext.getLane(nextLaneId);
            nextJunctionId = lane.getOutgoingJunction();
            precedingCar = lane.getFirstCar();
            // start searching precedingCar up to first crossroad TODO nextJunctionId.isCrossroad()
            while(precedingCar.isEmpty() && !nextJunctionId.isCrossroad()) {
                distance += lane.getLength();
                nextLaneId = route.getNextLaneId();
                lane = this.actorContext.getLane(nextLaneId);
                precedingCar = lane.getFirstCar();
            }
            distance += precedingCar.map(car -> car.getPosition() - car.getLength()).orElse(lane.getLength()); // distance to car or to crossroad
        }
        return new CarEnvironment(precedingCar, distance);
    }

    public double getPosition(){
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

    public CarId getCarId() {
        return this.id;
    }

    @Override
    public int compareTo(Car anotherCar) {
        return this.id.getValue().compareTo(anotherCar.getCarId().getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(id, car.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
