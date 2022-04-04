package pl.edu.agh.hiputs.model.car;

import pl.edu.agh.hiputs.model.actor.RoadStructureProvider;
import pl.edu.agh.hiputs.model.id.LaneId;

public interface CarReadWrite extends CarRead {

    void decide(RoadStructureProvider roadStructureProvider);

    CarUpdateResult update();

    Decision getDecision();

    RouteLocation getRouteLocation();

    void setPosition(double position);

    void setSpeed(double speed);

    void setRouteLocation(RouteLocation routeLocation);

    void setLocation(LaneLocation location);

    void setNewLocation(LaneId startLane);

}
