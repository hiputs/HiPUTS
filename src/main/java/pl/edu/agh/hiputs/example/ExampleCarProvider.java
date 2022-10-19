package pl.edu.agh.hiputs.example;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
public class ExampleCarProvider {

  private static final Double DEFAULT_CAR_LENGTH = 4.5;
  private static final Double DEFAULT_MAX_SPEED = 20.0;
  private static final Integer DEFAULT_HOPS = 4;
  private static final Double DEFAULT_MAX_DECELERATION = 3.5;
  private static final Double DEFAULT_TIME_STEP = 1.0;
  private static final Double DEFAULT_MAX_SPEED_SECURITY_FACTOR = 0.8;
  private final MapFragment mapFragment;
  private Function<JunctionId, List<LaneId>> junctionIdToOutgoingLaneIdList;
  private Function<LaneId, JunctionId> laneIdToOutgoingJunctionId;
  private List<LaneId> localLaneIdList;

  public ExampleCarProvider(MapFragment mapFragment) {
    this.mapFragment = mapFragment;
    this.localLaneIdList = mapFragment.getLocalLaneIds().stream().toList();

    this.junctionIdToOutgoingLaneIdList =
        junctionId -> mapFragment.getJunctionReadable(junctionId).streamOutgoingLaneIds().toList();

    this.laneIdToOutgoingJunctionId = laneId -> mapFragment.getLaneReadable(laneId).getOutgoingJunctionId();
  }

  Map<LaneId, PatchId> laneIdToPatchId = new HashMap<>();
  Map<JunctionId, PatchId> junctionIdPatchId = new HashMap<>();

  public ExampleCarProvider(MapFragment mapFragment, MapRepository mapRepository) {
    this.mapFragment = mapFragment;
    this.localLaneIdList = mapFragment.getLocalLaneIds().stream().toList();

    Queue<PatchId> notVisitedPatches = mapFragment.getKnownPatchReadable()
        .stream()
        .map(PatchReader::getPatchId)
        .collect(Collectors.toCollection(UniqueQueue::new));
    Set<PatchId> visitedPatches = new HashSet<>();
    while (!notVisitedPatches.isEmpty()) {
      PatchId currentPatchId = notVisitedPatches.poll();
      Patch currentPatch = mapRepository.getPatch(currentPatchId);
      currentPatch.getJunctionIds().forEach(junctionId -> junctionIdPatchId.put(junctionId, currentPatchId));
      currentPatch.getLaneIds().forEach(laneId -> laneIdToPatchId.put(laneId, currentPatchId));

      currentPatch.getNeighboringPatches()
          .stream()
          .filter(patchId -> !visitedPatches.contains(patchId))
          .forEach(notVisitedPatches::add);
      visitedPatches.add(currentPatchId);
    }

    this.junctionIdToOutgoingLaneIdList = junctionId -> mapRepository.getPatch(junctionIdPatchId.get(junctionId))
        .getJunctionReadable(junctionId)
        .streamOutgoingLaneIds()
        .toList();

    this.laneIdToOutgoingJunctionId = laneId -> mapRepository.getPatch(laneIdToPatchId.get(laneId))
          .getLaneReadable(laneId)
          .getOutgoingJunctionId();

  }

  public Car generateCar(int hops) {
    LaneId startLaneId = getRandomStartLaneId();
    return generateCar(startLaneId, hops);
  }

  public Car generateCar(LaneId startLaneId, int hops) {
    try {
      double position = ThreadLocalRandom.current().nextDouble(0, mapFragment.getLaneReadable(startLaneId).getLength());
      return this.generateCar(position, startLaneId, hops, DEFAULT_CAR_LENGTH, DEFAULT_MAX_SPEED);
    } catch (IllegalArgumentException e){
      log.debug("Error generating car");
      return null;
    }

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
    LaneId startLane = this.getRandomStartLaneId();
    return this.generateCar(position, startLane, hops, length, maxSpeed);
  }

  public Car generateCar(double position, LaneId startLaneId, int hops, double length, double maxSpeed) {
    RouteWithLocation routeWithLocation = this.generateRoute(startLaneId, hops);
    ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
    return Car.builder()
        .length(length)
        .maxSpeed(maxSpeed)
        .routeWithLocation(routeWithLocation)
        .laneId(startLaneId)
        .positionOnLane(position)
        .speed(threadLocalRandom.nextDouble(DEFAULT_MAX_SPEED))
        .build();
  }

  private RouteWithLocation generateRoute(LaneId startLaneId, int hops) {
    List<RouteElement> routeElements = new ArrayList<>();
    JunctionId startJunctionId = mapFragment.getLaneReadable(startLaneId).getIncomingJunctionId();
    routeElements.add(new RouteElement(startJunctionId, startLaneId));
    LaneId nextLaneId, laneId = startLaneId;
    JunctionId nextJunctionId , junctionId = startJunctionId;
    for (int i = 0; i < hops; i++) {
      nextJunctionId = this.laneIdToOutgoingJunctionId.apply(laneId);
      if (nextJunctionId == null) {
        break;
      }
      List<LaneId> junctionLaneIds = new LinkedList<>(this.junctionIdToOutgoingLaneIdList.apply(nextJunctionId));
      if (!nextJunctionId.isCrossroad()) {
        for(LaneId nextCandidateLaneId : new LinkedList<>(junctionLaneIds)) {
          if (this.laneIdToOutgoingJunctionId.apply(nextCandidateLaneId).equals(junctionId)) {
            junctionLaneIds.remove(nextCandidateLaneId);
          }
        }
      }
      nextLaneId = junctionLaneIds.get(ThreadLocalRandom.current().nextInt(junctionLaneIds.size()));
      routeElements.add(new RouteElement(nextJunctionId, nextLaneId));
      laneId = nextLaneId;
      junctionId = nextJunctionId;
    }

    return new RouteWithLocation(routeElements, 0);
  }

  private LaneId getRandomStartLaneId() {
    return this.localLaneIdList.get(ThreadLocalRandom.current().nextInt(this.localLaneIdList.size()));
  }

  public void limitSpeedPreventCollisionOnStart(Car currentCar, LaneReadable lane){
    Optional<CarReadable> carAtEntryOptional = lane.getCarAtEntryReadable();
    double distance;
    if(carAtEntryOptional.isPresent()) {
      CarReadable carAtEntry = carAtEntryOptional.get();
      double brakingDistance = carAtEntry.getSpeed() * carAtEntry.getSpeed() / DEFAULT_MAX_DECELERATION / 2;
      distance = carAtEntry.getPositionOnLane() - carAtEntry.getLength() + brakingDistance;
    }
    else{
      distance = lane.getLength();
    }
    distance -=  currentCar.getPositionOnLane();

    double maxSpeed = 0.0;
      //Limit maxSped cause car need to stop in integer number of time steps
    if(distance > 0){
      maxSpeed = Math.sqrt(distance * 2 * DEFAULT_MAX_DECELERATION) * DEFAULT_MAX_SPEED_SECURITY_FACTOR;
      double timeToStop = maxSpeed / DEFAULT_MAX_DECELERATION;
      timeToStop -= timeToStop % DEFAULT_TIME_STEP;
      maxSpeed = timeToStop * DEFAULT_MAX_DECELERATION * DEFAULT_MAX_SPEED_SECURITY_FACTOR;
    }

    if (currentCar.getSpeed() > maxSpeed){
      log.debug("Car: " + currentCar.getCarId() + " has reduced its speed before start from: " + currentCar.getSpeed() + " to: " + maxSpeed + ", distance: " + distance);
      currentCar.setSpeed(maxSpeed);
    }
  }

  private static class UniqueQueue<T> extends AbstractQueue<T> {
    private Queue<T> queue = new LinkedList<>();
    private Set<T> set = new HashSet<>();

    @Override
    public Iterator<T> iterator() {
      return queue.iterator();
    }

    @Override
    public int size() {
      return queue.size();
    }

    @Override
    public boolean add(T t) {
      if(set.add(t)) {
        return queue.add(t);
      }
      return false;
    }

    @Override
    public boolean offer(T t) {
      return add(t);
    }

    @Override
    public T poll() {
      T first = queue.poll();
      if (first != null) {
        set.remove(first);
      }
      return first;
    }

    @Override
    public T peek() {
      return queue.peek();
    }

  }
}
