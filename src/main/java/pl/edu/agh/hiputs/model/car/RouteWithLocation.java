package pl.edu.agh.hiputs.model.car;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.LaneId;

@Getter
@AllArgsConstructor
public class RouteWithLocation {

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
  // todo: remove this method?
  public RouteElement getNextRouteElement() {
    if (lastRouteElementReached()){
      throw new RouteExceededException("Cannot get next route element cause route end has been reached already");
    }
    return routeElements.get(currentPosition + 1);
  }

  /**
   * @return id of fist junction not yet visited. It should be same as junction id at the end of current lane.
   */
  // todo: remove this method?
  public JunctionId getNextJunctionId() {
    return getNextRouteElement().getJunctionId();
  }

  /**
   * @return id first lane not yet visited. It should be one of outgoingLanes from next junction on route.
   */
  // todo: remove this method?
  public LaneId getNextLaneId() {
    return getNextRouteElement().getOutgoingLaneId();
  }

  /**
   * @param offset by which increase car currentPosition
   *
   * @return offseted LaneId if exists or throws RouteExceededException if out of range
   */
  public Optional<LaneId> getOffsetLaneId(int offset) {
    if (currentPosition + offset >= this.routeElements.size() || currentPosition + offset < 0) {
      return Optional.empty();
    }
    return Optional.of(this.routeElements.get(currentPosition + offset).getOutgoingLaneId());
  }

  /**
   * Increment position on route by number of hops.
   */
  // todo: remove this method?
  public void moveForward(int hops) {
    setCurrentPosition(currentPosition + hops);
  }

  /**
   * Increment position on route by one route element.
   */
  // todo: remove this method?
  public void moveOneForward() {
    moveForward(1);
  }

  /**
   * Move index on route to given value or throw exception whether index is out of range
   *
   * @param currentPosition is index on route to be assigned
   */
  public boolean setCurrentPosition(int currentPosition) {
    if (currentPosition >= routeElements.size() || currentPosition < 0)
      return false;
    this.currentPosition = currentPosition;
    return true;
  }

  /**
   * @return True if currentPosition points to last element on route
   */
  public boolean lastRouteElementReached() {
    return currentPosition == routeElements.size() - 1;
  }

  public boolean moveCurrentPositionWithOffset(int offset) {
    return this.setCurrentPosition(this.currentPosition + offset);
  }

  private void validatePosition(int newPosition) throws RouteExceededException {
    if (newPosition >= routeElements.size() || newPosition < 0) {
      throw new RouteExceededException("Tried to access position = " + newPosition
          + " on route with size = " + routeElements.size());
    }
  }
}
