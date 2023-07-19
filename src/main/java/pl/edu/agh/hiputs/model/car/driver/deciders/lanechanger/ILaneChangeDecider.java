package pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger;

import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarPrecedingEnvironment;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

public interface ILaneChangeDecider {
  boolean makeDecision(CarReadable car, CarPrecedingEnvironment carPrecedingEnvironment, LaneId targetLaneId, RoadStructureReader roadStructureReader);
}
