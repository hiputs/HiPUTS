package pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.List;

@Component
@AllArgsConstructor
@ConditionalOnProperty(value = "route-generator.route-path-finder", havingValue = "dijkstra")
public class DijkstraRouteGenerator implements RouteGenerator{
  @Override
  public List<RouteWithLocation> generateRoutes(MapRepository mapRepository, Patch patch, int numberOfRoutes) {
//    to count patch length:
//    double patchLength = patch.streamLanesReadable().map(lane -> lane.getLength()).reduce(0.0, Double::sum);

//    TODO get starting lane, ending patch and lane randomly, but longer line -> higher chance

    return null;

  }
}
