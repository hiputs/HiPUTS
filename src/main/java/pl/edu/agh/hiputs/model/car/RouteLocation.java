package pl.edu.agh.hiputs.model.car;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

@Getter
@AllArgsConstructor
public class RouteLocation {

  /**
   * Route that car will follow
   * <p> contains List of RouteElements - effectively pairs of (junctionId, laneId) witch precisely indicates
   * what are consecutive object witch should be visited by car. Information where to go on next junction
   * could be simply obtained by checking actual position (laneId od junctionId) and finding it on route.
   * In order to reduce complexity of that kind of search, information of position on route is cached
   * and maintained in RouteLocation object situated in vehicle.
   * </p>
   */
  private final List<RouteElement> routeElements;

  /**
   * <p> Contains first routeElement index in path not yet visited by car. </p>
   * <p> JunctionId in this element is the same junctionId as outgoingJunctionId of lane where vehicle is currently
   * situated. </p>
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
    return routeElements.get(currentPosition);
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

  /**
   * @param offset by which increase car currentPosition
   *
   * @return offseted LaneId if exists or throws RouteExceededException if out of range
   */
  // TODO: return Optional<LaneId> and remove exception?
  public LaneId getOffsetLaneId(int offset) {
    if (currentPosition + offset >= this.routeElements.size() || currentPosition + offset < 0) {
      throw new RouteExceededException(
          "Size: " + this.routeElements.size() + " current position: " + currentPosition + " offset: " + offset);
    }
    return this.routeElements.get(currentPosition + offset).getOutgoingLaneId();
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
  // TODO: return boolean and remove exception?
  public void setCurrentPosition(int currentPosition) {
    if (currentPosition >= routeElements.size() || currentPosition < 0) {
      throw new RouteExceededException("Size: " + this.routeElements.size() + " new position: " + currentPosition);
    }
    this.currentPosition = currentPosition;
  }

  public void moveCurrentPositionWithOffset(int offset) {
    this.setCurrentPosition(this.currentPosition + offset);
  }
}

class RouteExceededException extends RuntimeException {

  public RouteExceededException(String message) {
    super(message);
  }
}