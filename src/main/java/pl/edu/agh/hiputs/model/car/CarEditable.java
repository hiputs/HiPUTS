package pl.edu.agh.hiputs.model.car;

import java.util.Optional;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

public interface CarEditable extends CarReadable, Comparable<CarEditable> {

  void decide(RoadStructureReader roadStructureReader);

  Optional<CarUpdateResult> update();

  Decision getDecision();

  RouteWithLocation getRouteWithLocation();

  void setPositionOnRoadAndSpeed(double position, double speed);
}
