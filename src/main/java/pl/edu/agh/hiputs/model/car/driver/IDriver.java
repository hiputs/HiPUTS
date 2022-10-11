package pl.edu.agh.hiputs.model.car.driver;

import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

/** To implement a new FunctionalDecider you have to:
 implement FunctionalDecider interface
 and add it to MainDecider you already using in Car
 */
public interface IDriver {
  Decision makeDecision(CarReadable car, RoadStructureReader roadStructureReader);

  double getDistanceHeadway();
}
