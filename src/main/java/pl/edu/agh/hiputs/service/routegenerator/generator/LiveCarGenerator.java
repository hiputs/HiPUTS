package pl.edu.agh.hiputs.service.routegenerator.generator;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.car.driver.Driver;
import pl.edu.agh.hiputs.model.car.driver.DriverParameters;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator.RouteGenerator;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.List;
import java.util.Random;

@Component
@AllArgsConstructor
@ConditionalOnProperty(value = "car-generator.generator-source", havingValue = "live")
public class LiveCarGenerator implements CarGenerator {

  private final Configuration configuration;
  private final GeneratorCarAmountProvider carAmountProvider;
  private final RouteGenerator routeGenerator;
  private final MapRepository mapRepository;
  private final Random random = new Random();


  @Override
  public List<Car> generateCars(Patch patch, int step, MapFragment mapFragment) {
    var patchTotalLaneLength = patch.getLanesLength();
    var carsAmountToGenerate = carAmountProvider.getCarsToGenerateAmountAtStep(step, patchTotalLaneLength);
    List<RouteWithLocation> routes = configuration.isTestMode() ?
      routeGenerator.generateRoutesFromMapFragment(patch, carsAmountToGenerate, mapFragment) :
      routeGenerator.generateRoutesFromMapRepository(patch, carsAmountToGenerate, mapRepository);

    return routes.stream().map(route -> {
      var speed = random.nextDouble(0, 100);
      var carLength = random.nextDouble(3.0, 5.0);
      var maxSpeed = random.nextDouble(speed, speed + 20.0);
      return Car.builder()
        .length(carLength)
        .speed(speed)
        .maxSpeed(maxSpeed)
        .routeWithLocation(route)
        .laneId(route.getRouteElements().get(0).getOutgoingLaneId())
        .positionOnLane(0)
        .driver(new Driver(new DriverParameters(configuration)))
        .build();
    }).toList();
  }

}
