package pl.edu.agh.hiputs.model.car.driver.deciders.lights;

import java.util.Optional;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.CarEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.JunctionDecision;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

public interface TrafficLightsDecider {

  Optional<JunctionDecision> tryToMakeDecision(
      CarReadable car,
      CarEnvironment carEnvironment,
      RoadStructureReader roadStructureReader
  );

}
