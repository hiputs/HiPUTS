package pl.edu.agh.hiputs.service.routegenerator.generator;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.driver.Driver;
import pl.edu.agh.hiputs.model.car.driver.DriverParameters;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.ConfigurationService;

import java.util.List;

@Component
@AllArgsConstructor
@ConditionalOnProperty(value = "car-generator.generator-source", havingValue = "file")
public class FileCarGenerator implements CarGenerator {

  private final RouteReader routeReader;

  @Override
  public List<Car> generateCars(Patch patch, int step, MapFragment mapFragment) {
    return routeReader.readNextRoutes(patch.getPatchId(), step).stream().map(routeEntry ->
      {
        var route = routeEntry.getRoute();
        var startLaneId = route.getRouteElements().get(0).getOutgoingLaneId();
        return Car.builder()
          .length(routeEntry.getCarLength())
          .maxSpeed(routeEntry.getMaxSpeed())
          .routeWithLocation(route)
          .laneId(startLaneId)
          .positionOnLane(0)
          .speed(routeEntry.getSpeed())
          .driver(new Driver(new DriverParameters(ConfigurationService.getConfiguration())))
          .build();
      }
    ).toList();
  }
}
