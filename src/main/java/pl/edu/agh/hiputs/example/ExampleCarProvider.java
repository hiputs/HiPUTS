package pl.edu.agh.hiputs.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;

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
    this.junctionIdToOutgoingLaneIdList =
        junctionId -> mapFragment.getJunctionReadable(junctionId).streamOutgoingLaneIds().toList();

    this.laneIdToOutgoingJunctionId = laneId -> mapFragment.getLaneReadable(laneId).getOutgoingJunctionId();

    this.localLaneIdList = mapFragment.getLocalLaneIds().stream().toList();
  }

  public Car generateCar() {
    return this.generateCar(DEFAULT_HOPS, DEFAULT_CAR_LENGTH, DEFAULT_MAX_SPEED);
  }

  public Car generateCar(LaneId startLaneId, int hops) {
    return this.generateCar(startLaneId, hops, DEFAULT_CAR_LENGTH, DEFAULT_MAX_SPEED);
  }

  public Car generate(int hops) {
    return this.generateCar(hops, DEFAULT_CAR_LENGTH, DEFAULT_MAX_SPEED);
  }

  public Car generateCar(int hops, double length, double maxSpeed) {
    LaneId startLaneId = this.getRandomStartLaneId();
    return this.generateCar(startLaneId, hops, length, maxSpeed);
  }

  public Car generateCar(LaneId startLaneId, int hops, double length, double maxSpeed) {
    RouteWithLocation routeWithLocation = this.generateRoute(startLaneId, hops);
    ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
    return Car.builder()
        .length(length)
        .maxSpeed(maxSpeed)
        .routeWithLocation(routeWithLocation)
        .laneId(startLaneId)
        .positionOnLane(threadLocalRandom.nextDouble(mapFragment.getLaneReadable(startLaneId).getLength()))
        .speed(threadLocalRandom.nextDouble(DEFAULT_MAX_SPEED))
        .build();
  }

  private RouteWithLocation generateRoute(LaneId startLaneId, int hops) {
    List<RouteElement> routeElements = new ArrayList<>();
    LaneId nextLaneId, laneId = startLaneId;
    JunctionId junctionId;
    for (int i = 0; i < hops; i++) {
      junctionId = this.laneIdToOutgoingJunctionId.apply(laneId);
      if (junctionId == null) {
        break;
      }
      List<LaneId> junctionLaneIds = this.junctionIdToOutgoingLaneIdList.apply(junctionId);
      nextLaneId = junctionLaneIds.get(ThreadLocalRandom.current().nextInt(junctionLaneIds.size()));
      routeElements.add(new RouteElement(junctionId, nextLaneId));
      laneId = nextLaneId;
    }
    return new RouteWithLocation(routeElements, 0);
  }

  private LaneId getRandomStartLaneId() {
    return this.localLaneIdList.get(ThreadLocalRandom.current().nextInt(this.localLaneIdList.size()));
  }
}
