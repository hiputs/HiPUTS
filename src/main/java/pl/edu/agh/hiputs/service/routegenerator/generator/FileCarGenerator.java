package pl.edu.agh.hiputs.service.routegenerator.generator;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.driver.Driver;
import pl.edu.agh.hiputs.model.car.driver.DriverParameters;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.ConfigurationService;

import java.util.List;

@Component
@AllArgsConstructor
@ConditionalOnProperty(value = "car-generator.generate-from-file", havingValue = "true")
public class FileCarGenerator implements CarGenerator {

  private final RouteReader routeReader;

  @Override
  public List<Car>  generateCars(Patch patch, int step) {
    return routeReader.readNextRoute(patch.getPatchId(), step).map(route -> {
      return Car.builder()
        // randomowe wartosci - do zmiany
        .length(4.5)
        .maxSpeed(ConfigurationService.getConfiguration().getDefaultMaxSpeed())
        .routeWithLocation(route)
        .laneId(route.getRouteElements().get(0).getOutgoingLaneId())
        .positionOnLane(0)
        .speed(ConfigurationService.getConfiguration().getDefaultMaxSpeed())
        .driver(new Driver(new DriverParameters(ConfigurationService.getConfiguration())))
        .build();
    }).stream().toList();
  }
}
