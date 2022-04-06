package pl.edu.agh.hiputs.model.map.example;

import pl.edu.agh.hiputs.model.actor.MapFragment;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.Route;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteLocation;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * Example car generator for maps from ExampleMapFragmentProvider
 * all generators need car starting position
 */
public class ExampleCarProvider {

    private static final Double DEFAULT_CAR_LENGTH = 4.5;
    private static final Double DEFAULT_MAX_SPEED = 20.0;
    private static final Integer DEFAULT_HOPS = 4;

    private final MapFragment mapFragment;
    private Function<JunctionId, List<LaneId>> junctionIdToOutgoingLaneIdList;
    private Function<LaneId, JunctionId> laneIdToOutgoingJunctionId;
    private List<LaneId> localLaneIdList;

    public ExampleCarProvider(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
        readPatches(mapFragment);
    }

    void readPatches(MapFragment mapFragment) {
        this.junctionIdToOutgoingLaneIdList = junctionId ->
                mapFragment.getJunctionReadable(junctionId).streamOutgoingLaneIds().toList();

        this.laneIdToOutgoingJunctionId = laneId ->
                mapFragment.getLaneReadable(laneId).getOutgoingJunction();

        this.localLaneIdList = mapFragment.getLocalLaneIds().stream().toList();
    }

    public Car generateCar(double position) {
        return this.generateCar(position, DEFAULT_HOPS, DEFAULT_CAR_LENGTH, DEFAULT_MAX_SPEED);
    }

    public Car generateCar(double position, LaneId startLane, int hops) {
        return this.generateCar(position, startLane, hops, DEFAULT_CAR_LENGTH, DEFAULT_MAX_SPEED);
    }

    public Car generate(double position, int hops) {
        return this.generateCar(position, hops, DEFAULT_CAR_LENGTH, DEFAULT_MAX_SPEED);
    }

    public Car generateCar(double position, int hops, double length, double maxSpeed) {
        LaneId startLane = this.getRandomStartLane();
        return this.generateCar(position, startLane, hops, length, maxSpeed);
    }

    public Car generateCar(double position, LaneId startLaneId, int hops, double length, double maxSpeed) {
        RouteLocation routeLocation = this.generateRoute(startLaneId, hops);
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        return Car.builder()
                .length(length)
                .maxSpeed(maxSpeed)
                .routeLocation(routeLocation)
                .laneId(startLaneId)
                .positionOnLane(position)
                .speed(threadLocalRandom.nextDouble(DEFAULT_MAX_SPEED))
                .build();
    }

    private RouteLocation generateRoute(LaneId startLane, int hops) {
        List<RouteElement> routeElements = new ArrayList<>();
        LaneId nextLaneId, laneId = startLane;
        JunctionId junctionId;
        for (int i = 0; i < hops; i++) {
            junctionId = this.laneIdToOutgoingJunctionId.apply(laneId);
            if (junctionId == null)
                break;
            List<LaneId> junctionLanes = this.junctionIdToOutgoingLaneIdList.apply(junctionId);
            nextLaneId = junctionLanes.get(ThreadLocalRandom.current().nextInt(junctionLanes.size()));
            routeElements.add(new RouteElement(junctionId, nextLaneId));
            laneId = nextLaneId;
        }
        return new RouteLocation(new Route(routeElements));
    }

    private LaneId getRandomStartLane() {
        return this.localLaneIdList.get(ThreadLocalRandom.current().nextInt(this.localLaneIdList.size()));
    }
}
