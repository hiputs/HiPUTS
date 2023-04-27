package pl.edu.agh.hiputs.service.routegenerator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.exception.EntityNotFoundException;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.routegenerator.generator.CarGenerator;

import static java.text.MessageFormat.format;

@Slf4j
@AllArgsConstructor
@Service
@ConditionalOnProperty(value = "car-generator.generate-from-file", havingValue = "true")
public class CarGeneratorServiceImpl implements CarGeneratorService {

  private final CarGenerator carGenerator;

  @Override
  public void generateCars(MapFragment fragment, int step) {
    fragment.localPatches().forEach(patch -> generateCarsForPatch(patch, step));
  }

  private void generateCarsForPatch(Patch patch, int step) {
    var cars = carGenerator.generateCars(patch, step);
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
