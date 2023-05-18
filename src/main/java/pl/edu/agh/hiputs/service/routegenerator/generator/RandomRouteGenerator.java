package pl.edu.agh.hiputs.service.routegenerator.generator;

import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.List;
import java.util.Random;

public class RandomRouteGenerator implements RouteGenerator{

  private Patch getRandomPatchFromMap(MapRepository mapRepository) {
    Random rand = new Random();
    return mapRepository.getAllPatches().get(rand.nextInt(mapRepository.getAllPatches().size()));
  }

  @Override
  public List<RouteWithLocation> generateRoute(MapRepository mapRepository, Patch patch, int numberOfRoutes) {
//    to count patch length:
//    double patchLength = patch.streamLanesReadable().map(lane -> lane.getLength()).reduce(0.0, Double::sum);

//    TODO get starting lane, ending patch and lane randomly, but longer line -> higher chance

    final LaneId startingLaneId = patch.getAnyLane().getLaneId();

    final LaneId endLaneId = getRandomPatchFromMap(mapRepository).getAnyLane().getLaneId();



    return null;

  }
}
