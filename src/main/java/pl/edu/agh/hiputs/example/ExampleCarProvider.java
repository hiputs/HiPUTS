package pl.edu.agh.hiputs.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
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
        .collect(Collectors.toCollection(LinkedList::new));
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
    double position = ThreadLocalRandom.current().nextDouble(0, mapFragment.getLaneReadable(startLaneId).getLength());
    return this.generateCar(position, startLaneId, DEFAULT_HOPS, DEFAULT_CAR_LENGTH, DEFAULT_MAX_SPEED);
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
    routeElements.add(new RouteElement(null, startLaneId));
    LaneId nextLaneId, laneId = startLaneId;
    JunctionId junctionId , prevJunctionId = null;
    for (int i = 0; i < hops; i++) {
      junctionId = this.laneIdToOutgoingJunctionId.apply(laneId);
      if (junctionId == null) {
        break;
      }
      List<LaneId> junctionLaneIds = new LinkedList<>(this.junctionIdToOutgoingLaneIdList.apply(junctionId));
      if (!junctionId.isCrossroad()) {
        for(LaneId nextCandidateLaneId : junctionLaneIds) {
          if (this.laneIdToOutgoingJunctionId.apply(nextCandidateLaneId) == prevJunctionId) {
            junctionLaneIds.remove(nextCandidateLaneId);
          }
        }
      }
      nextLaneId = junctionLaneIds.get(ThreadLocalRandom.current().nextInt(junctionLaneIds.size()));
      routeElements.add(new RouteElement(junctionId, nextLaneId));
      laneId = nextLaneId;
      prevJunctionId = junctionId;
    }
    return new RouteWithLocation(routeElements, 0);
  }

  private LaneId getRandomStartLaneId() {
    return this.localLaneIdList.get(ThreadLocalRandom.current().nextInt(this.localLaneIdList.size()));
  }
}
