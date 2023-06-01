package pl.edu.agh.hiputs.example;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
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
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
public class ExampleCarProvider {

  private final MapFragment mapFragment;
  private Function<JunctionId, List<RoadId>> junctionIdToOutgoingRoadIdList;
  private Function<RoadId, JunctionId> roadIdToOutgoingJunctionId;
  private Function<RoadId, Set<RoadId>> roadIdToSuccessorRoadIds;
  private List<RoadId> localRoadIdList;
  private List<LaneId> localLaneIdList;
  private Configuration configuration;

  public ExampleCarProvider(MapFragment mapFragment) {
    this.mapFragment = mapFragment;
    this.localRoadIdList = mapFragment.getLocalRoadIds().stream().toList();
    this.localLaneIdList = mapFragment.getLocalLaneIds().stream().toList();

    this.junctionIdToOutgoingRoadIdList =
        junctionId -> mapFragment.getJunctionReadable(junctionId).streamOutgoingRoadIds().toList();

    this.roadIdToOutgoingJunctionId = roadId -> mapFragment.getRoadReadable(roadId).getOutgoingJunctionId();
    this.roadIdToSuccessorRoadIds = roadId -> mapFragment.getRoadToLaneSuccessorsReadable(roadId)
        .stream()
        .map(LaneReadable::getRoadId)
        .collect(Collectors.toSet());
    this.configuration = ConfigurationService.getConfiguration();
  }

  public ExampleCarProvider(MapFragment mapFragment, MapRepository mapRepository) {
    this.mapFragment = mapFragment;
    this.localRoadIdList = mapFragment.getLocalRoadIds().stream().toList();
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
      currentPatch.getRoadIds().forEach(roadId -> roadIdToPatchId.put(roadId, currentPatchId));
      currentPatch.getLaneIds().forEach(laneId -> laneIdToPatchId.put(laneId, currentPatchId));

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

    //TODO: move to mapRepository?
    this.roadIdToSuccessorRoadIds = roadId -> mapRepository.getPatch(roadIdToPatchId.get(roadId))
        .getRoadReadable(roadId)
        .getLanes()
        .stream()
        .map(laneId -> mapRepository.getPatch(laneIdToPatchId.get(laneId))
              .getLaneReadable(laneId).getLaneSuccessors())
        .flatMap(Collection::stream)
        .map(laneId -> mapRepository.getPatch(laneIdToPatchId.get(laneId))
            .getLaneReadable(laneId).getRoadId())
        .collect(Collectors.toSet());

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
  Map<LaneId, PatchId> laneIdToPatchId = new HashMap<>();
  Map<JunctionId, PatchId> junctionIdPatchId = new HashMap<>();

  public Car generateCar(int hops) {
    LaneId startLaneId = getRandomStartLaneId();
    return generateCar(startLaneId, hops);
  }

  public Car generateCar(LaneId startLaneId, int hops) {
    try {
      double position = ThreadLocalRandom.current().nextDouble(0, mapFragment.getRoadReadableFromLaneId(startLaneId).getLength());
      return this.generateCar(position, startLaneId, hops, getDefaultCarLength(), getDefaultMaxSpeed());
    } catch (IllegalArgumentException e){
      log.warn("Error generating car", e);
      return null;
    }
  }

  public Car generateCar(double position) {
    return this.generateCar(position, getDefaultHops(), getDefaultCarLength(), getDefaultMaxSpeed());
  }
  public Car generateCar(double position, LaneId startLane) {
    return this.generateCar(position, startLane, getDefaultHops(), getDefaultCarLength(), getDefaultMaxSpeed());
  }

  public Car generateCar(double position, LaneId startLane, int hops) {
    return this.generateCar(position, startLane, hops, getDefaultCarLength(), getDefaultMaxSpeed());
  }

  public Car generate(double position, int hops) {
    return this.generateCar(position, hops, getDefaultCarLength(), getDefaultMaxSpeed());
  }

  public Car generateCar(double position, int hops, double length, double maxSpeed) {
    LaneId startLane = this.getRandomStartLaneId();
    return this.generateCar(position, startLane, hops, length, maxSpeed);
  }

  public Car generateCar(double position, LaneId startLaneId, int hops, double length, double maxSpeed) {
    RoadId startRoadId = mapFragment.getLaneReadable(startLaneId).getRoadId();
    RouteWithLocation routeWithLocation = this.generateRoute(startLaneId, hops);
    ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
    return Car.builder()
        .length(length)
        .maxSpeed(maxSpeed)
        .routeWithLocation(routeWithLocation)
        .roadId(startRoadId)
        .laneId(startLaneId)
        .positionOnLane(position)
        .speed(threadLocalRandom.nextDouble(getDefaultMaxSpeed()))
        .driver(new Driver(new DriverParameters(configuration)))
        .build();
  }

  private RouteWithLocation generateRoute(LaneId startLaneId, int hops) {
    List<RouteElement> routeElements = new ArrayList<>();
    RoadReadable road = mapFragment.getRoadReadableFromLaneId(startLaneId);
    JunctionId startJunctionId = road.getIncomingJunctionId();
    routeElements.add(new RouteElement(startJunctionId, road.getRoadId()));
    RoadId nextRoadId, roadId = road.getRoadId();
    JunctionId nextJunctionId , junctionId = startJunctionId;
    for (int i = 0; i < hops; i++) {
      nextJunctionId = this.roadIdToOutgoingJunctionId.apply(roadId);
      if (nextJunctionId == null) {
        break;
      }
      List<RoadId> junctionRoadIds = new LinkedList<>(this.junctionIdToOutgoingRoadIdList.apply(nextJunctionId));
      HashSet<RoadId> successorRoadIds = new HashSet<>(this.roadIdToSuccessorRoadIds.apply(roadId));
      if (!nextJunctionId.isCrossroad()) {
        for(RoadId nextCandidateRoadId : new LinkedList<>(junctionRoadIds)) {
          if (this.roadIdToOutgoingJunctionId.apply(nextCandidateRoadId).equals(junctionId)) {
            junctionRoadIds.remove(nextCandidateRoadId);
          }
          if (!successorRoadIds.contains(nextCandidateRoadId)) {
            junctionRoadIds.remove(nextCandidateRoadId);
          }
        }
      }
      if (junctionRoadIds.isEmpty()) {
        break;
      }
      nextRoadId = junctionRoadIds.get(ThreadLocalRandom.current().nextInt(junctionRoadIds.size()));
      routeElements.add(new RouteElement(nextJunctionId, nextRoadId));
      roadId = nextRoadId;
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
      double brakingDistance = carAtEntry.getSpeed() * carAtEntry.getSpeed() / getMaxDeceleration() / 2;
      distance = carAtEntry.getPositionOnLane() - carAtEntry.getLength() + brakingDistance;
    }
    else{
      distance = lane.getLength();
    }
    distance -=  currentCar.getPositionOnLane();

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
