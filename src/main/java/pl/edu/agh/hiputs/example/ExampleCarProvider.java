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
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.car.driver.Driver;
import pl.edu.agh.hiputs.model.car.driver.DriverParameters;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
public class ExampleCarProvider {

  private final MapFragment mapFragment;
  private Function<JunctionId, List<RoadId>> junctionIdToOutgoingRoadIdList;
  private Function<RoadId, JunctionId> roadIdToOutgoingJunctionId;
  private List<RoadId> localRoadIdList;
  private Configuration configuration;

  public ExampleCarProvider(MapFragment mapFragment) {
    this.mapFragment = mapFragment;
    this.localRoadIdList = mapFragment.getLocalRoadIds().stream().toList();

    this.junctionIdToOutgoingRoadIdList =
        junctionId -> mapFragment.getJunctionReadable(junctionId).streamOutgoingRoadIds().toList();

    this.roadIdToOutgoingJunctionId = roadId -> mapFragment.getRoadReadable(roadId).getOutgoingJunctionId();
    this.configuration = ConfigurationService.getConfiguration();
  }

  public ExampleCarProvider(MapFragment mapFragment, MapRepository mapRepository) {
    this.mapFragment = mapFragment;
    this.localRoadIdList = mapFragment.getLocalRoadIds().stream().toList();

    Queue<PatchId> notVisitedPatches = mapFragment.getKnownPatchReadable()
        .stream()
        .map(PatchReader::getPatchId)
        .collect(Collectors.toCollection(UniqueQueue::new));
    Set<PatchId> visitedPatches = new HashSet<>();
    while (!notVisitedPatches.isEmpty()) {
      PatchId currentPatchId = notVisitedPatches.poll();
      Patch currentPatch = mapRepository.getPatch(currentPatchId);
      currentPatch.getJunctionIds().forEach(junctionId -> junctionIdPatchId.put(junctionId, currentPatchId));
      currentPatch.getRoadIds().forEach(roadId -> roadIdToPatchId.put(roadId, currentPatchId));

      currentPatch.getNeighboringPatches()
          .stream()
          .filter(patchId -> !visitedPatches.contains(patchId))
          .forEach(notVisitedPatches::add);
      visitedPatches.add(currentPatchId);
    }

    this.junctionIdToOutgoingRoadIdList = junctionId -> mapRepository.getPatch(junctionIdPatchId.get(junctionId))
        .getJunctionReadable(junctionId)
        .streamOutgoingRoadIds()
        .toList();

    this.roadIdToOutgoingJunctionId = roadId -> mapRepository.getPatch(roadIdToPatchId.get(roadId))
        .getRoadReadable(roadId)
        .getOutgoingJunctionId();

    this.configuration = ConfigurationService.getConfiguration();
  }

  private Double getDefaultCarLength() {
    return 4.5; //DEFAULT_CAR_LENGTH;
  }

  private Double getDefaultMaxSpeed() {
    return configuration.getDefaultMaxSpeed();
  }

  private Integer getDefaultHops() {
    return 4; //DEFAULT_HOPS;
  }

  private Double getMaxDeceleration() {
    return configuration.getMaxDeceleration();
  }

  private Double getTimeStep() {
    return configuration.getSimulationTimeStep();
  }

  private Double getDefaultMaxSpeedSecurityFactor() {
    return 0.8; //DEFAULT_MAX_SPEED_SECURITY_FACTOR;
  }

  Map<RoadId, PatchId> roadIdToPatchId = new HashMap<>();
  Map<JunctionId, PatchId> junctionIdPatchId = new HashMap<>();

  public Car generateCar(int hops) {
    RoadId startRoadId = getRandomStartRoadId();
    return generateCar(startRoadId, hops);
  }

  public Car generateCar(RoadId startRoadId, int hops) {
    try {
      double position = ThreadLocalRandom.current().nextDouble(0, mapFragment.getRoadReadable(startRoadId).getLength());
      return this.generateCar(position, startRoadId, hops, getDefaultCarLength(), getDefaultMaxSpeed());
    } catch (IllegalArgumentException e){
      log.warn("Error generating car", e);
      return null;
    }
  }

  public Car generateCar(double position) {
    return this.generateCar(position, getDefaultHops(), getDefaultCarLength(), getDefaultMaxSpeed());
  }
  public Car generateCar(double position, RoadId startRoad) {
    return this.generateCar(position, startRoad, getDefaultHops(), getDefaultCarLength(), getDefaultMaxSpeed());
  }

  public Car generateCar(double position, RoadId startRoad, int hops) {
    return this.generateCar(position, startRoad, hops, getDefaultCarLength(), getDefaultMaxSpeed());
  }

  public Car generate(double position, int hops) {
    return this.generateCar(position, hops, getDefaultCarLength(), getDefaultMaxSpeed());
  }

  public Car generateCar(double position, int hops, double length, double maxSpeed) {
    RoadId startRoad = this.getRandomStartRoadId();
    return this.generateCar(position, startRoad, hops, length, maxSpeed);
  }

  public Car generateCar(double position, RoadId startRoadId, int hops, double length, double maxSpeed) {
    RouteWithLocation routeWithLocation = this.generateRoute(startRoadId, hops);
    ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
    return Car.builder()
        .length(length)
        .maxSpeed(maxSpeed)
        .routeWithLocation(routeWithLocation)
        .roadId(startRoadId)
        .positionOnRoad(position)
        .speed(threadLocalRandom.nextDouble(getDefaultMaxSpeed()))
        .driver(new Driver(new DriverParameters(configuration)))
        .build();
  }

  private RouteWithLocation generateRoute(RoadId startRoadId, int hops) {
    List<RouteElement> routeElements = new ArrayList<>();
    JunctionId startJunctionId = mapFragment.getRoadReadable(startRoadId).getIncomingJunctionId();
    routeElements.add(new RouteElement(startJunctionId, startRoadId));
    RoadId nextRoadId, roadId = startRoadId;
    JunctionId nextJunctionId , junctionId = startJunctionId;
    for (int i = 0; i < hops; i++) {
      nextJunctionId = this.roadIdToOutgoingJunctionId.apply(roadId);
      if (nextJunctionId == null) {
        break;
      }
      List<RoadId> junctionRoadIds = new LinkedList<>(this.junctionIdToOutgoingRoadIdList.apply(nextJunctionId));
      if (!nextJunctionId.isCrossroad() && junctionRoadIds.size() >= 2) {
        for(RoadId nextCandidateRoadId : new LinkedList<>(junctionRoadIds)) {
          if (this.roadIdToOutgoingJunctionId.apply(nextCandidateRoadId).equals(junctionId)) {
            junctionRoadIds.remove(nextCandidateRoadId);
          }
        }
      }
      nextRoadId = junctionRoadIds.get(ThreadLocalRandom.current().nextInt(junctionRoadIds.size()));
      routeElements.add(new RouteElement(nextJunctionId, nextRoadId));
      roadId = nextRoadId;
      junctionId = nextJunctionId;
    }

    return new RouteWithLocation(routeElements, 0);
  }

  private RoadId getRandomStartRoadId() {
    return this.localRoadIdList.get(ThreadLocalRandom.current().nextInt(this.localRoadIdList.size()));
  }

  public void limitSpeedPreventCollisionOnStart(Car currentCar, RoadReadable road){
    Optional<CarReadable> carAtEntryOptional = road.getCarAtEntryReadable();
    double distance;
    if(carAtEntryOptional.isPresent()) {
      CarReadable carAtEntry = carAtEntryOptional.get();
      double brakingDistance = carAtEntry.getSpeed() * carAtEntry.getSpeed() / getMaxDeceleration() / 2;
      distance = carAtEntry.getPositionOnRoad() - carAtEntry.getLength() + brakingDistance;
    }
    else{
      distance = road.getLength();
    }
    distance -=  currentCar.getPositionOnRoad();

    double maxSpeed = 0.0;
      //Limit maxSped cause car need to stop in integer number of time steps
    if(distance > 0){
      maxSpeed = Math.sqrt(distance * 2 * getMaxDeceleration()) * getDefaultMaxSpeedSecurityFactor();
      double timeToStop = maxSpeed / getMaxDeceleration();
      timeToStop -= timeToStop % getTimeStep();
      maxSpeed = timeToStop * getMaxDeceleration() * getDefaultMaxSpeedSecurityFactor();
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
