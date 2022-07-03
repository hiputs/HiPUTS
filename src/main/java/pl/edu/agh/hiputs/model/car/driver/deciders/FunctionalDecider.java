package pl.edu.agh.hiputs.model.car.driver.deciders;

import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

/**
 * More information in Driver class
 */

public interface FunctionalDecider {

  double makeDecision(CarReadable managedCar, CarEnvironment environment, RoadStructureReader roadStructureReader);

}
