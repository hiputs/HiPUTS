package pl.edu.agh.hiputs.model.car;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Configurable;
import pl.edu.agh.hiputs.model.actor.RoadStructureReader;
import pl.edu.agh.hiputs.model.follow.IDMDecider;
import pl.edu.agh.hiputs.model.follow.IDecider;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.LaneRead;

import java.util.Objects;
import java.util.Optional;

@Configurable
@Getter
@Builder
@AllArgsConstructor
public class
Car implements CarReadWrite, Comparable<Car> {

    /**
     * Unique car identifier.
     */
    @Builder.Default
    private final CarId id = new CarId();
    /**
     * Length of the car.
     */
    @Builder.Default
    private final double length = 5;
    /**
     * Maximum possible speed of the car.
     */
    @Builder.Default
    private final double maxSpeed = 20;
    /**
     * Decider instance
     */
    @Builder.Default
    private final IDecider decider = new IDMDecider();
    /**
     * Lane on which car is currently situated.
     */
    private LaneId laneId;
    /**
     * Position of car at its lane.
     */
    private double positionOnLane;
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
     * Current acceleration of car.
     */
    @Builder.Default
    private double acceleration = 0;
    /**
     * Decision on how car state should be changed. Calculated by decider.
     */
    private Decision decision;

    public void decide(RoadStructureReader roadStructureReader) {
        // make local decision based on read only road structure (watch environment) and save it locally

        //First prepare CarEnvironment
        CarEnvironment environment = this.getPrecedingCar(roadStructureReader);

        double acceleration = this.decider.makeDecision(this, environment);

        LaneId currentLaneId = this.laneId;
        LaneRead destinationCandidate = roadStructureReader.getLaneReadById(currentLaneId);
        int offset = -1;
        double desiredPosition = calculateFuturePosition();

        while (desiredPosition > destinationCandidate.getLength()) {
            desiredPosition -= destinationCandidate.getLength();
            offset++;
            currentLaneId = routeLocation.getOffsetLaneId(offset);
            destinationCandidate = roadStructureReader.getLaneReadById(currentLaneId);
        }

        decision = Decision.builder()
                .acceleration(acceleration)
                .speed(this.speed + acceleration)
                .laneId(currentLaneId)
                .positionOnLane(desiredPosition)
                .offsetToMoveOnRoute(offset + 1)
                .build();
    }

    public CarUpdateResult update() {
        this.routeLocation.moveCurrentPositionWithOffset(decision.getOffsetToMoveOnRoute());
        this.speed = decision.getSpeed();
        this.acceleration = decision.getAcceleration();
        CarUpdateResult carUpdateResult = new CarUpdateResult(
                this.laneId,
                decision.getLaneId(),
                decision.getPositionOnLane()
        );
        this.laneId = decision.getLaneId();
        this.positionOnLane = decision.getPositionOnLane();
        return carUpdateResult;
    }

    /**
     * Search for preceding car or crossroad
     * on the way counting distance to car or to crossroad
     *
     * @return CarEnvironment
     */
    public CarEnvironment getPrecedingCar(RoadStructureReader roadStructureReader) {
        LaneRead currentLane = roadStructureReader.getLaneReadById(this.laneId);
        JunctionId nextJunctionId = currentLane.getOutgoingJunction();
        Optional<CarRead> precedingCar = currentLane.getNextCarData(this);
        Optional<JunctionId> nextCrossroadId;
        double distance;
        if (nextJunctionId.isCrossroad() || precedingCar.isPresent())
            distance = precedingCar
                    .map(car -> car.getPositionOnLane() - car.getLength())
                    .orElse(currentLane.getLength()) - this.positionOnLane;
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
                nextLane = roadStructureReader.getLaneReadById(nextLaneId);
                nextJunctionId = nextLane.getOutgoingJunction();
                precedingCar = nextLane.getFirstCar();
                currentLane = nextLane;
            }
            distance += precedingCar
                    .map(car -> car.getPositionOnLane() - car.getLength())
                    .orElse(currentLane.getLength()) - this.positionOnLane;
        }
        if (nextJunctionId.isCrossroad())
            nextCrossroadId = Optional.of(nextJunctionId);
        else
            nextCrossroadId = Optional.empty();
        return new CarEnvironment(precedingCar, nextCrossroadId, distance);
    }

    public double calculateFuturePosition() {
        return this.positionOnLane + this.speed + this.acceleration / 2;
    }

    public RouteLocation getRouteLocation() {
        return routeLocation;
    }

    public void setRouteLocation(RouteLocation routeLocation) {
        this.routeLocation = routeLocation;
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

}
