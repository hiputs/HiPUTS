package pl.edu.agh.model.map.example;

import lombok.Getter;
import pl.edu.agh.model.actor.MapFragment;
import pl.edu.agh.model.car.Car;
import pl.edu.agh.model.car.Route;
import pl.edu.agh.model.car.RouteElement;
import pl.edu.agh.model.car.RouteLocation;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.Junction;
import pl.edu.agh.model.map.LaneReadWrite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
public class ExampleCarProvider {

    private static final Double DEFAULT_CAR_LENGTH = 4.5;
    private static final Double DEFAULT_MAX_SPEED = 20.0;
    private static final Integer DEFAULT_HOPS = 4;

    private Map<JunctionId, List<LaneId>> junctionId2outgoingLaneId;
    private Map<LaneId, JunctionId> laneId2outgoingJunctionId;
    private Map<LaneId, LaneReadWrite> laneId2laneReadWrite;
    private List<LaneId> laneIdList;

    public ExampleCarProvider(MapFragment mapFragment) {
        readPatches(mapFragment);
    }

    void readPatches(MapFragment mapFragment) {
        this.junctionId2outgoingLaneId = mapFragment.getLocalPatches().stream()
                .flatMap(patch -> patch.getJunctions().values().stream())
                .collect(Collectors.toMap(Junction::getId, junction -> new ArrayList<>(junction.getOutgoingLanesIds())));

        this.laneId2outgoingJunctionId = mapFragment.getLocalPatches().stream()
                .flatMap(patch -> patch.getLanes().values().stream())
                .collect(Collectors.toMap(LaneReadWrite::getId, LaneReadWrite::getOutgoingJunction));

        this.laneId2laneReadWrite = mapFragment.getLocalPatches().stream()
                .flatMap(patch -> patch.getLanes().values().stream())
                .collect(Collectors.toMap(LaneReadWrite::getId, laneReadWrite -> laneReadWrite));

        this.laneIdList = mapFragment.getLocalPatches().stream()
                .flatMap(patch -> patch.getLanes().keySet().stream())
                .collect(Collectors.toList());
    }

    public Car generateCar() {
        return this.generateCar(DEFAULT_HOPS, DEFAULT_CAR_LENGTH, DEFAULT_MAX_SPEED);
    }

    public Car generateCar(LaneId startLane, int hops) {
        return this.generateCar(startLane, hops, DEFAULT_CAR_LENGTH, DEFAULT_MAX_SPEED);
    }

    public Car generate(int hops) {
        return this.generateCar(hops, DEFAULT_CAR_LENGTH, DEFAULT_MAX_SPEED);
    }

    public Car generateCar(int hops, double length, double maxSpeed) {
        LaneId startLane = this.getRandomStartLane();
        return this.generateCar(startLane, hops, length, maxSpeed);
    }

    public Car generateCar(LaneId startLane, int hops, double length, double maxSpeed) {
        RouteLocation routeLocation = this.generateRoute(startLane, hops);
        Car car = new Car(length, maxSpeed, routeLocation);
        car.setNewLocation(startLane);
        car.setPosition(ThreadLocalRandom.current().nextDouble(laneId2laneReadWrite.get(startLane).getLength()));
        car.setSpeed(ThreadLocalRandom.current().nextDouble(DEFAULT_MAX_SPEED));
        return car;
    }

    private RouteLocation generateRoute(LaneId startLane, int hops) {
        List<RouteElement> routeElements = new ArrayList<>();
        LaneId nextLaneId, laneId = startLane;
        JunctionId junctionId;
        for (int i = 0; i < hops; i++) {
            junctionId = this.laneId2outgoingJunctionId.get(laneId);
            if (junctionId == null)
                break;
            List<LaneId> junctionLanes = this.junctionId2outgoingLaneId.get(junctionId);
            nextLaneId = junctionLanes.get(ThreadLocalRandom.current().nextInt(junctionLanes.size()));
            routeElements.add(new RouteElement(junctionId, nextLaneId));
            laneId = nextLaneId;
        }
        return new RouteLocation(new Route(routeElements));
    }

    private LaneId getRandomStartLane() {
        return this.laneIdList.get(ThreadLocalRandom.current().nextInt(this.laneIdList.size()));
    }
}
