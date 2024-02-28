package pl.edu.agh.hiputs.service.routegenerator.generator;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.driver.Driver;
import pl.edu.agh.hiputs.model.car.driver.DriverParameters;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Component
@AllArgsConstructor
@ConditionalOnProperty(value = "car-generator.generator-source", havingValue = "file")
public class FileCarGenerator implements CarGenerator {

  private final RouteReader routeReader;

  @Override
  public List<Car> generateCars(Patch patch, int step, MapFragment mapFragment) {
    return routeReader.readNextRoutes(patch.getPatchId(), step).stream().map(routeEntry -> {
      var route = routeEntry.getRoute();
      var startRoadId = route.getRouteElements().get(0).getOutgoingRoadId();
      var lanesOfStartRoute = mapFragment.getRoadReadable(startRoadId).getLanes();
      return Car.builder()
          .length(routeEntry.getCarLength())
          .maxSpeed(routeEntry.getMaxSpeed())
          .routeWithLocation(route)
          .roadId(startRoadId)
          .laneId(lanesOfStartRoute.get(ThreadLocalRandom.current().nextInt(lanesOfStartRoute.size())))
          .positionOnLane(0)
          .speed(routeEntry.getSpeed())
          .driver(new Driver(new DriverParameters(ConfigurationService.getConfiguration())))
          .build();
    }).toList();
  }
}
