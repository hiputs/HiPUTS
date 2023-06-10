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


  private double patchLength(Patch patch){
//    System.out.println(patch);
//    System.out.println(patch.streamLanesReadable().findAny().get().getLength());
    System.out.println(patch.streamLanesReadable().map(LaneReadable::getLength).reduce(0.0, Double::sum));
    return patch.streamLanesReadable().map(LaneReadable::getLength).reduce(0.0, Double::sum);
  }

  private LaneId getRandomWeightedLaneId(Patch patch){
    double totalLength = patchLength(patch);
    double startingPoint = ThreadLocalRandom.current().nextDouble(totalLength);

    for(LaneId laneId : patch.getLaneIds()) {
      startingPoint -= patch.getLaneReadable(laneId).getLength();
      if (startingPoint <= 0){
        return laneId;
      }
    }

    throw new NoLaneFoundException(patch.getPatchId().getValue());
  }

  private Patch getEndingPatch(MapRepository mapRepository){
    System.out.println("rozmiar1:" + mapRepository.getAllPatches().size());
    System.out.println("rozmiar2: " + mapRepository.getAllPatches().stream().map(patch -> patchLength(patch)).count());
    double totalLength = mapRepository.getAllPatches().stream().map(patch -> patchLength(patch)).reduce(0.0, Double::sum);


    System.out.println(totalLength);
    double startingPoint = ThreadLocalRandom.current().nextDouble(totalLength);

    for(Patch patch : mapRepository.getAllPatches()) {
      startingPoint -= patchLength(patch);
      if (startingPoint <= 0){
        return patch;
      }
    }

    throw new NoPatchFoundException();

  }

  @Override
  public List<RouteWithLocation> generateRoutes(MapRepository mapRepository, Patch startPatch, int numberOfRoutes, MapFragment mapFragment) {
//    TODO: sprawdzić na mapie z nie pustym MapRepository
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    CHBidirectionalDijkstra pathFinder = new CHBidirectionalDijkstra(mapFragment, executor);


    List<RouteWithLocation> routes = new ArrayList<>();
    for (int i=0;i < numberOfRoutes; i++){
      try{
        LaneId startLaneId = getRandomWeightedLaneId(startPatch);

//        Patch endPatch = getEndingPatch(mapRepository);
//        LaneId endLineId = getRandomWeightedLaneId(endPatch);

        LaneId endLineId = startPatch.getJunctionReadable(startPatch.getLaneReadable(startLaneId).getIncomingJunctionId()).streamIncomingLaneIds().findAny().get();

//        System.out.println(startLaneId + " end: " +  endLineId);
        System.out.println(Pair.of(startLaneId, endLineId));

        routes.add(pathFinder.getPath(Pair.of(startLaneId, endLineId)));

      } catch (NoLaneFoundException e) {
        log.error("Error while finding lane in patch: " + e.toString());
      }
      catch (NoPatchFoundException e) {
        log.error("Error while finding patch in mapRepository");
      }
    }

    return routes;
  }
}
