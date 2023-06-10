package pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Component
@AllArgsConstructor
@ConditionalOnProperty(value = "route-generator.route-path-finder", havingValue = "random")
public class RandomRouteGenerator implements RouteGenerator{

  private JunctionId getOutgoingJunctionId(LaneId laneId, Patch patch){
    return patch.getLaneReadable(laneId).getOutgoingJunctionId();
  }

  private List<LaneId> getOutgoingLaneIdList(JunctionId junctionId, Patch patch){
    return patch.getJunctionReadable(junctionId).streamOutgoingLaneIds().toList();
  }

  private RouteWithLocation generateRoute(Patch patch, LaneId startLaneId, int hops) {
//    TODO zmienić tymczasowe generowanie wewnątrz patcha (przechodzenie po sąsiadujących patchach zmiana patcha jesli
//     lane wyjdzie poza patch, można sie wzrorowac na funkji MapRepository::getPatchIdByLaneId)
    List<RouteElement> routeElements = new ArrayList<>();
    JunctionId startJunctionId = patch.getLaneReadable(startLaneId).getIncomingJunctionId();
    routeElements.add(new RouteElement(startJunctionId, startLaneId));
    LaneId nextLaneId, laneId = startLaneId;
    JunctionId nextJunctionId , junctionId = startJunctionId;
    for (int i = 0; i < hops; i++) {
      nextJunctionId = getOutgoingJunctionId(laneId, patch);
      if (nextJunctionId == null) {
        break;
      }
      List<LaneId> junctionLaneIds = new LinkedList<>(getOutgoingLaneIdList(nextJunctionId, patch));
      if (!nextJunctionId.isCrossroad()) {
        for(LaneId nextCandidateLaneId : new LinkedList<>(junctionLaneIds)) {
          if (getOutgoingJunctionId(nextCandidateLaneId, patch).equals(junctionId)) {
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

  @Override
  public List<RouteWithLocation> generateRoutes(MapRepository mapRepository, Patch patch, int numberOfRoutes, MapFragment mapFragment) {

    List<RouteWithLocation> routes = new LinkedList<>();
    List<LaneId> lanes = patch.getLaneIds().stream().toList();

    if(!lanes.isEmpty()){
      for (int routeNum = 0; routeNum < numberOfRoutes; routeNum++) {
        LaneId startLaneId = lanes.get(ThreadLocalRandom.current().nextInt(lanes.size()));
        int hops = ThreadLocalRandom.current().nextInt(2,8);
//      Losowa wartość !!!
        routes.add(generateRoute(patch, startLaneId, hops));
      }
    }

    return routes;
  }
}
