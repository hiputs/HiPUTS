package pl.edu.agh.hiputs.model.car.driver.deciders.lanechanger;

import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

public interface ILaneChangeChecker {

  MobilModelDecision makeDecision(CarReadable car, LaneId targetLaneId, double politenessFactor,
      RoadStructureReader roadStructureReader);
}
