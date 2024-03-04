package pl.edu.agh.hiputs.service.routegenerator;

import static java.text.MessageFormat.format;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.configuration.Configuration;
import pl.edu.agh.hiputs.exception.EntityNotFoundException;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.routegenerator.generator.CarGenerator;

@Slf4j
@AllArgsConstructor
@Service
@ConditionalOnProperty(value = "car-generator.new-generator", havingValue = "true")
public class CarGeneratorServiceImpl implements CarGeneratorService {

  private final CarGenerator carGenerator;
  private final FileGeneratorService fileGeneratorService;
  private final Configuration configuration;

  @Override
  public void generateInitialCars(MapFragment mapFragment) {
    if (configuration.getCarGenerator().isGenerateRouteFiles()) {
      fileGeneratorService.generateFiles(mapFragment);
    }
    manageCars(mapFragment, 0);
  }

  @Override
  public void manageCars(MapFragment fragment, int step) {
    fragment.getLocalPatches().forEach(patch -> generateCarsForPatch(patch, step, fragment));
  }

  private void generateCarsForPatch(Patch patch, int step, MapFragment mapFragment) {
    var cars = carGenerator.generateCars(patch, step, mapFragment);
    log.debug("{} cars are being placed on patch {} in {} step", cars.size(), patch, step);
    cars.forEach(car -> placeCarOnPatch(car, patch));
  }

  private void placeCarOnPatch(Car car, Patch patch) {
    try {
      patch.placeCar(car);
    } catch (EntityNotFoundException e) {
      log.error(format("Car {} cannot be placed on patch {}", car, patch));
    }
  }
}
