package pl.edu.agh.hiputs.model.follow;

import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

public interface FunctionalDecider {

  double makeDecision(CarReadable managedCar, CarEnvironment environment, RoadStructureReader roadStructureReader);

}
