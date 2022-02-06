package pl.edu.agh.model.car;

import lombok.RequiredArgsConstructor;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.LaneId;

import java.util.Objects;

@RequiredArgsConstructor
public class RouteLocation {

    /*
     * Route that car will follow
     */
    private final Route route;

    /**
     * <p> Contains first routeElement index in path not yet visited by car. </p>
     * <p> JunctionId in this element is the same junctionId as outgoingJunctionId of lane where vehicle is currently situated. </p>
     * <p> Example: </p>
     * <p> map: Lane1 -> JunctionX -> Lane2 </p>
     * <p> car is on Lane1 </p>
     * <p> current position points to element (JunctionX, Lane2)</p>
     */
    private int currentPosition = 0;

    /**
     * @return RouteElement starting with not yet visited junction
     */
    public RouteElement getNextRouteElement() {
        return route.getRouteElements().get(currentPosition);
    }

    /**
     * @return id of fist junction not yet visited. It should be same as junction id at the end of current lane.
     */
    public JunctionId getNextJunctionId() {
        return getNextRouteElement().getJunctionId();
    }

    /**
     * @return id first lane not yet visited. It should be one of outgoingLanes from next junction on route.
     */
    public LaneId getNextLaneId() {
        return getNextRouteElement().getOutgoingLaneId();
    }

    /*
     * Increment position on route by one route element.
     */
    public void moveOneForward() {
        setCurrentPosition(currentPosition + 1);
    }

    /**
     * Move index on route to given value or throw exception whether index is out of range
     *
     * @param currentPosition is index on route to be assigned
     */
    public void setCurrentPosition(int currentPosition) {
        Objects.checkIndex(currentPosition, this.route.getRouteElements().size());
        this.currentPosition = currentPosition;
    }
}
