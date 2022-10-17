package pl.edu.agh.hiputs.model.car.driver.deciders.junction;

import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

public interface JunctionDecider {

  JunctionDecision makeDecision(CarReadable managedCar, CarEnvironment environment, RoadStructureReader roadStructureReader);

}
