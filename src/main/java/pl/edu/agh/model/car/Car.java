package pl.edu.agh.model.car;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Configurable;
import pl.edu.agh.model.actor.RoadStructureProvider;
import pl.edu.agh.model.follow.IDecider;
import pl.edu.agh.model.id.CarId;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.LaneRead;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

@Configurable
@Getter
@Builder
@AllArgsConstructor
public class Car implements CarReadWrite, Comparable<Car> {

    /**
     * Unique car identifier.
     */
    @Builder.Default
    private CarId id = new CarId();

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
    @Builder.Default
    private double speed = 0;

    /**
     * Length of the car.
     */
    @Builder.Default
    private double length = 5;

    /**
     * Maximum possible speed of the car.
     */
    @Builder.Default
    private double maxSpeed = 20;

    /**
     * Current acceleration of car.
     */
    @Builder.Default
    private double acceleration = 0;

    /**
     * Decision on how car state should be changed. Calculated by decider.
     */
    private Decision decision;

    /**
     * Decider instance
     */
    @Inject
    private IDecider decider;

    public Car() {
        this.id = new CarId();
    }

    public Car(double length, double maxSpeed, RouteLocation routeLocation) {
        this();
        this.length = length;
        this.maxSpeed = maxSpeed;
        this.routeLocation = routeLocation;
    }

    public void decide(RoadStructureProvider roadStructureProvider) {
        // make local decision based on read only road structure (watch environment) and save it locally

        //First prepare CarEnvironment
        CarEnvironment environment = this.getPrecedingCar(roadStructureProvider);

        double acceleration = this.decider.makeDecision(this, environment);

        LaneId currentLaneId = location.getLane();
        LaneRead destinationCandidate = roadStructureProvider.getLaneReadById(currentLaneId);
        int offset = -1;
        double desiredPosition = calculateFuturePosition();

        while (desiredPosition > destinationCandidate.getLength()) {
            desiredPosition -= destinationCandidate.getLength();
            offset++;
            currentLaneId = routeLocation.getOffsetLaneId(offset);
            destinationCandidate = roadStructureProvider.getLaneReadById(currentLaneId);
        }

        decision = Decision.builder()
                .acceleration(acceleration)
                .speed(this.speed + acceleration)
                .location(new LaneLocation(currentLaneId, desiredPosition))
                .offsetToMoveOnRoute(offset + 1)
                .build();
    }

    public CarUpdateResult update() {
        this.routeLocation.moveCurrentPositionWithOffset(decision.getOffsetToMoveOnRoute());
        this.speed = decision.getSpeed();
        this.acceleration = decision.getAcceleration();
        CarUpdateResult carUpdateResult = new CarUpdateResult();
        carUpdateResult.setOldLaneId(this.location.getLane());
        this.location = decision.getLocation();
        carUpdateResult.setNewLaneLocation(this.location);
        return carUpdateResult;
    }

    /**
     * Search for preceding car or crossroad
     * on the way counting distance to car or to crossroad
     *
     * @return CarEnvironment
     */
    public CarEnvironment getPrecedingCar(RoadStructureProvider roadStructureProvider) {
        LaneRead currentLane = roadStructureProvider.getLaneReadById(this.location.getLane());
        JunctionId nextJunctionId = currentLane.getOutgoingJunction();
        Optional<CarRead> precedingCar = currentLane.getNextCarData(this);
        Optional<JunctionId> nextCrossroadId;
        double distance;
        if (nextJunctionId.isCrossroad() || precedingCar.isPresent())
            distance = precedingCar
                    .map(car -> car.getPosition() - car.getLength())
                    .orElse(currentLane.getLength()) - this.getPosition();
        else {
            distance = 0;
            int offset = 0;
            LaneId nextLaneId;
            LaneRead nextLane;
            while (precedingCar.isEmpty() && !nextJunctionId.isCrossroad()) {
                try {
                    nextLaneId = routeLocation.getOffsetLaneId(offset++);
                } catch (RouteExceededException routeExceededException) {
                    break;
                }
                distance += currentLane.getLength(); // adds previous lane length
                nextLane = roadStructureProvider.getLaneReadById(nextLaneId);
                nextJunctionId = nextLane.getOutgoingJunction();
                precedingCar = nextLane.getFirstCar();
                currentLane = nextLane;
            }
            distance += precedingCar
                    .map(car -> car.getPosition() - car.getLength())
                    .orElse(currentLane.getLength()) - this.getPosition();
        }
        if (nextJunctionId.isCrossroad())
            nextCrossroadId = Optional.of(nextJunctionId);
        else
            nextCrossroadId = Optional.empty();
        return new CarEnvironment(precedingCar, nextCrossroadId, distance);
    }

    public double calculateFuturePosition() {
        return getPosition() + this.speed + this.acceleration / 2;
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

    @Override
    public CarId getId() {
        return id;
    }

    public RouteLocation getRouteLocation() {
        return routeLocation;
    }

    public void setRouteLocation(RouteLocation routeLocation) {
        this.routeLocation = routeLocation;
    }

    public void setLocation(LaneLocation location) {
        this.location = location;
    }

    public Decision getDecision() {
        return decision;
    }

    @Override
    public int compareTo(Car anotherCar) {
        return this.id.getValue().compareTo(anotherCar.getId().getValue());
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

    public void setNewLocation(LaneId startLane) {
        this.location = new LaneLocation();
        this.location.setLane(startLane);
    }

}
