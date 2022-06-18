package pl.edu.agh.hiputs.model.follow;

import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

/** To implement a new FunctionalDecider you have to:
 implement FunctionalDecider interface
 and add it to MainDecider you already using in Car
 */
public interface MainDecider {
  double makeDecision(CarReadable car, RoadStructureReader roadStructureReader);
}
