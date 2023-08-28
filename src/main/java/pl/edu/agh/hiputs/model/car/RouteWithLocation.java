package pl.edu.agh.hiputs.model.car;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.model.id.RoadId;

@Getter
@AllArgsConstructor
public class RouteWithLocation {

  /**
   * Route that car will follow
   * <p> contains List of RouteElements - effectively pairs of (junctionId, roadId) witch precisely indicates
   * what are consecutive object witch should be visited by car. Information where to go on next junction
   * could be simply obtained by checking actual position (roadId od junctionId) and finding it on route.
   * In order to reduce complexity of that kind of search, information of position on route is cached
   * and maintained in RouteLocation object situated in vehicle.
   * </p>
   */
  private final List<RouteElement> routeElements;

  /**
   * <p> Contains first routeElement index in path not yet visited by car. </p>
   * <p> JunctionId in this element is the same junctionId as outgoingJunctionId of road where vehicle is currently
   * situated. </p>
   * <p> Example: </p>
   * <p> map: Road1 -> JunctionX -> Road2 </p>
   * <p> car is on Road1 </p>
   * <p> current position points to element (JunctionX, Road2)</p>
   */
  private int currentPosition = 0;

  /**
   * @param offset by which increase car currentPosition
   *
   * @return offseted RoadId if exists or throws RouteExceededException if out of range
   */
  public Optional<RoadId> getOffsetRoadId(int offset) {
    if (currentPosition + offset >= this.routeElements.size() || currentPosition + offset < 0) {
      return Optional.empty();
    }
    return Optional.of(this.routeElements.get(currentPosition + offset).getOutgoingRoadId());
  }
 
  /**
   * Increment position on route by number of hops.
   */
  public boolean moveForward(int hops) {
    int futurePosition = currentPosition + hops;
    if (futurePosition >= routeElements.size() || futurePosition < 0) {
      return false;
    } else {
      this.currentPosition = futurePosition;
      return true;
    }
  }

  public String toString(){
    return "current position: " + currentPosition + ", " + routeElements.stream()
        .map(el -> " jun: "+ el.getJunctionId() + " road: " + el.getOutgoingRoadId() + " -> ").toList();
  }

  public int getRemainingRouteSize() {
    return routeElements.size() - currentPosition - 1;
  }

  public RouteElement getLastRouteElement() {
    return routeElements.get(routeElements.size() - 1);
  }

  public synchronized void addRouteElements(List<RouteElement> extension) {
    routeElements.addAll(extension);
  }
}
