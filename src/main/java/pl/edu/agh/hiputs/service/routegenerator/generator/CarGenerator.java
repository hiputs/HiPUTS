package pl.edu.agh.hiputs.service.routegenerator.generator;

import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;

import java.util.List;

public interface CarGenerator {

  List<Car> generateCars(Patch patch, int step, MapFragment mapFragment);

}
