package pl.edu.agh.hiputs.model.car;

import java.util.Optional;
import pl.edu.agh.hiputs.model.car.driver.deciders.junction.CrossroadDecisionProperties;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.RoadId;

public interface CarReadable {

  double getPositionOnRoad();

  RoadId getRoadId();

  double getLength();

  double getAcceleration();

  double getSpeed();

  double getMaxSpeed();

  CarId getCarId();

  Optional<CrossroadDecisionProperties> getCrossRoadDecisionProperties();

  Optional<RoadId> getRouteOffsetRoadId(int offset);

  double getDistanceHeadway();
}
