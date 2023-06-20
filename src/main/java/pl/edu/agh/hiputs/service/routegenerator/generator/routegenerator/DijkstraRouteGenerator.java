package pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.alg.util.Pair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.service.pathfinder.CHBidirectionalDijkstra;
import pl.edu.agh.hiputs.service.pathfinder.PathFinder;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnProperty(value = "route-generator.route-path-finder", havingValue = "dijkstra")
public class DijkstraRouteGenerator implements RouteGenerator{

  private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

  @Override
  public List<RouteWithLocation> generateRoutesFromMapFragment(Patch startPatch, int numberOfRoutes, MapFragment mapFragment) {
    CHBidirectionalDijkstra pathFinder = new CHBidirectionalDijkstra(mapFragment, executor);
    return generateRoutes(startPatch, numberOfRoutes, mapFragment.localPatches(), pathFinder);
  }

  @Override
  public List<RouteWithLocation> generateRoutesFromMapRepository(Patch startPatch, int numberOfRoutes, MapRepository mapRepository) {
    CHBidirectionalDijkstra pathFinder = new CHBidirectionalDijkstra(mapRepository, executor);
    return generateRoutes(startPatch, numberOfRoutes, mapRepository.getAllPatches(), pathFinder);
  }

  private List<RouteWithLocation> generateRoutes(Patch startPatch, int numberOfRoutes, List<Patch> patches, PathFinder pathFinder){
    List<RouteWithLocation> routes = new ArrayList<>();
    for (int i=0;i < numberOfRoutes; i++){
      try{
        LaneId startLaneId = getRandomWeightedLaneId(startPatch);

        Patch endPatch = getEndingPatch(patches);
        LaneId endLineId = getRandomWeightedLaneId(endPatch);

        routes.add(pathFinder.getPath(Pair.of(startLaneId, endLineId)));

      } catch (NoLaneFoundException e) {
        log.error("Error while finding lane in patch: " + e.getMessage());
      }
      catch (NoPatchFoundException e) {
        log.error("Error while finding patch in mapRepository");
      }
    }

    return routes;
  }


  private LaneId getRandomWeightedLaneId(Patch patch){
    double totalLength = patch.getLanesLength();
    double startingPoint = ThreadLocalRandom.current().nextDouble(totalLength);

    for(LaneId laneId : patch.getLaneIds()) {
      startingPoint -= patch.getLaneReadable(laneId).getLength();
      if (startingPoint <= 0){
        return laneId;
      }
    }

    throw new NoLaneFoundException(patch.getPatchId().getValue());
  }

  private Patch getEndingPatch(List<Patch> patches){
    double totalLength = patches.stream().map(Patch::getLanesLength).reduce(0.0, Double::sum);

    double startingPoint = ThreadLocalRandom.current().nextDouble(totalLength);

    for(Patch patch : patches) {
      startingPoint -= patch.getLanesLength();
      if (startingPoint <= 0){
        return patch;
      }
    }

    throw new NoPatchFoundException();
  }
}
