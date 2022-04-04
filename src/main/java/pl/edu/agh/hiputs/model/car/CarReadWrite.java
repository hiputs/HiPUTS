package pl.edu.agh.hiputs.model.car;

import pl.edu.agh.hiputs.model.actor.RoadStructureReader;

public interface CarReadWrite extends CarRead {

    void decide(RoadStructureReader roadStructureReader);

    CarUpdateResult update();

    Decision getDecision();

    RouteLocation getRouteLocation();

    void setRouteLocation(RouteLocation routeLocation);

}
