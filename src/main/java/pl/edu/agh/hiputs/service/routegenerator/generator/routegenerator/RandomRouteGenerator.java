package pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
@AllArgsConstructor
@ConditionalOnProperty(value = "route-generator.route-generator-type", havingValue = "random")
public class RandomRouteGenerator implements RouteGenerator {

  @Override
  public List<RouteWithLocation> generateRoutesFromMapFragment(Patch patch, int numberOfRoutes,
      MapFragment mapFragment) {
    return generateRandomRoutes(patch, numberOfRoutes);
  }

  @Override
  public List<RouteWithLocation> generateRoutesFromMapRepository(Patch patch, int numberOfRoutes,
      MapRepository mapRepository) {
    return generateRandomRoutes(patch, numberOfRoutes);
  }

  private List<RouteWithLocation> generateRandomRoutes(Patch patch, int numberOfRoutes) {
    List<RouteWithLocation> routes = new LinkedList<>();
    List<LaneId> lanesIds = patch.getLaneIds().stream().toList();

    if (!lanesIds.isEmpty()) {
      for (int routeNum = 0; routeNum < numberOfRoutes; routeNum++) {
        LaneId startLaneId = lanesIds.get(ThreadLocalRandom.current().nextInt(lanesIds.size()));
        int hops = ThreadLocalRandom.current().nextInt(2, 8);
        routes.add(generateRoute(patch, startLaneId, hops));
      }
    }

    return routes;
  }

  private RouteWithLocation generateRoute(Patch patch, LaneId startLaneId,
      int hops) { // todo refactor - very similar code in ExampleCarProvider>generateRouteElements()
    List<RouteElement> routeElements = new ArrayList<>();

    LaneReadable lane = patch.getLaneReadable(startLaneId);
    RoadReadable road = patch.getRoadReadable(lane.getRoadId());

    JunctionId startJunctionId = road.getIncomingJunctionId();
    routeElements.add(new RouteElement(startJunctionId, road.getRoadId()));
    // LaneId nextLaneId, laneId = startLaneId;
    RoadId nextRoadId, roadId = road.getRoadId();
    var junctionId = startJunctionId;
    var nextJunctionIdOpt = Optional.of(startJunctionId);
    for (int i = 0; i < hops; i++) {
      nextJunctionIdOpt = getOutgoingJunctionId(roadId, patch);
      if (nextJunctionIdOpt.isEmpty()) {
        break;
      }
      var nextJunctionId = nextJunctionIdOpt.get();
      List<RoadId> junctionRoadIds = new LinkedList<>(getOutgoingRoadIdList(nextJunctionId, patch));
      if (!nextJunctionId.isCrossroad()) {
        for (RoadId nextCandidateRoadId : new LinkedList<>(junctionRoadIds)) {
          var junctionIdFinal = junctionId;
          if (getOutgoingJunctionId(nextCandidateRoadId, patch).map(j -> j.equals(junctionIdFinal)).orElse(false)) {
            junctionRoadIds.remove(nextCandidateRoadId);
          }
        } //todo there is another if-check in ExampleCarProvider>generateRouteElements() - check whether needed
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

  private Optional<JunctionId> getOutgoingJunctionId(RoadId roadId,
      Patch patch) { // todo -bugs?- what if roadId is not in this patch? imo MapRepository is needed (get patch
    // based on roadId)
    return Optional.ofNullable(patch.getRoadReadable(roadId)).map(RoadReadable::getOutgoingJunctionId);
  }

  private List<RoadId> getOutgoingRoadIdList(JunctionId junctionId, Patch patch) {
    return patch.getJunctionReadable(junctionId).streamOutgoingRoadIds().toList();
  }
}
