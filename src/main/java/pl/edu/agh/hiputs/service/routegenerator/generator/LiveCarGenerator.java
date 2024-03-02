package pl.edu.agh.hiputs.service.routegenerator.generator;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.configuration.Configuration;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.car.driver.Driver;
import pl.edu.agh.hiputs.model.car.driver.DriverParameters;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator.RouteGenerator;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

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
    List<RouteWithLocation> routes =
        configuration.isTestMode() ? routeGenerator.generateRoutesFromMapFragment(patch, carsAmountToGenerate,
            mapFragment) : routeGenerator.generateRoutesFromMapRepository(patch, carsAmountToGenerate, mapRepository);

    return routes.stream().map(route -> {
      var carLength =
          random.nextDouble(configuration.getCarMinLengthInMeters(), configuration.getCarMaxLengthInMeters());
      var speed = random.nextDouble(0, configuration.getCarUpSpeedBoundaryInMetersPerSecond());
      var maxSpeed = random.nextDouble(speed, configuration.getCarUpMaxSpeedBoundaryInMetersPerSecond());
      var startRoadId = route.getRouteElements().get(0).getOutgoingRoadId();
      var lanesOfStartRoute = mapFragment.getRoadReadable(startRoadId).getLanes();
      return Car.builder()
          .length(carLength)
          .speed(speed)
          .maxSpeed(maxSpeed)
          .routeWithLocation(route)
          .roadId(startRoadId)
          .laneId(lanesOfStartRoute.get(ThreadLocalRandom.current().nextInt(lanesOfStartRoute.size())))
          .positionOnLane(0)
          .driver(new Driver(new DriverParameters(configuration)))
          .build();
    }).toList();
  }

}
