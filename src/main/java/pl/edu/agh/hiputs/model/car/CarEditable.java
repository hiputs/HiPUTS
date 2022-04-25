package pl.edu.agh.hiputs.model.car;

import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

public interface CarEditable extends CarReadable, Comparable<CarEditable> {

  void decide(RoadStructureReader roadStructureReader);

  CarUpdateResult update();

  Decision getDecision();

  RouteWithLocation getRouteWithLocation();

}
