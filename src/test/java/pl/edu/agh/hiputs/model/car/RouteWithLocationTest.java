package pl.edu.agh.hiputs.model.car;

import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.RouteMatcher.Route;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;

public class RouteWithLocationTest {

  private static final List<JunctionId> orderedJunctionsOnRoute = new ArrayList<>();
  private static final List<LaneId> orderedLaneIdsOnRoute = new ArrayList<>();
  private static List<RouteElement> routeElements;
  private static int elementCount = 5;

  @BeforeAll
  public static void setup() {
    routeElements = new ArrayList<>();
    for (int i = 0; i < elementCount; i++) {
      JunctionId junctionId = new JunctionId(String.valueOf(i), JunctionType.BEND);
      LaneId laneId = new LaneId(String.valueOf(i));
      orderedJunctionsOnRoute.add(junctionId);
      orderedLaneIdsOnRoute.add(laneId);
      routeElements.add(new RouteElement(junctionId, laneId));
    }
  }

  @Test
  public void getNextRouteElementTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    Assertions.assertThat(routeWithLocation.getRouteElements().get(routeWithLocation.getCurrentPosition()))
        .isEqualTo(new RouteElement(orderedJunctionsOnRoute.get(0), orderedLaneIdsOnRoute.get(0)));
  }

  @Test
  public void getNextJunctionIdTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    Assertions.assertThat(
            routeWithLocation.getRouteElements().get(routeWithLocation.getCurrentPosition()).getJunctionId())
        .isEqualTo(orderedJunctionsOnRoute.get(0));
  }

  @Test
  public void getNextLaneIdTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    Assertions.assertThat(
            routeWithLocation.getRouteElements().get(routeWithLocation.getCurrentPosition()).getOutgoingLaneId())
        .isEqualTo(orderedLaneIdsOnRoute.get(0));
  }

  @Test
  public void moveOneForwardTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    Assertions.assertThat(routeWithLocation.moveForward(1)).isTrue();
    Assertions.assertThat(
            routeWithLocation.getRouteElements().get(routeWithLocation.getCurrentPosition()).getJunctionId())
        .isEqualTo(orderedJunctionsOnRoute.get(1));
    Assertions.assertThat(
            routeWithLocation.getRouteElements().get(routeWithLocation.getCurrentPosition()).getOutgoingLaneId())
        .isEqualTo(orderedLaneIdsOnRoute.get(1));
  }

  @Test
  public void moveForwardReturnFalseTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    Assertions.assertThat(routeWithLocation.moveForward(elementCount)).isFalse();
  }

  @Test
  public void traversWholeRouteByOneMoveForwardOnlyTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    for (int i = 1; i < 5; i++) {
      Assertions.assertThat(routeWithLocation.moveForward(1)).isTrue();
      Assertions.assertThat(
              routeWithLocation.getRouteElements().get(routeWithLocation.getCurrentPosition()).getJunctionId())
          .isEqualTo(orderedJunctionsOnRoute.get(i));
      Assertions.assertThat(
              routeWithLocation.getRouteElements().get(routeWithLocation.getCurrentPosition()).getOutgoingLaneId())
          .isEqualTo(orderedLaneIdsOnRoute.get(i));
    }
  }

  @Test
  public void moveForwardTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    Assertions.assertThat(routeWithLocation.moveForward(4)).isTrue();
    Assertions.assertThat(
            routeWithLocation.getRouteElements().get(routeWithLocation.getCurrentPosition()).getJunctionId())
        .isEqualTo(orderedJunctionsOnRoute.get(4));
    Assertions.assertThat(
            routeWithLocation.getRouteElements().get(routeWithLocation.getCurrentPosition()).getOutgoingLaneId())
        .isEqualTo(orderedLaneIdsOnRoute.get(4));
  }

  @Test
  public void getOffsetLaneIdTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    Assertions.assertThat(routeWithLocation.getOffsetLaneId(1).get()).isEqualTo(routeElements.get(1).getOutgoingLaneId());
  }

  @Test
  public void getOffsetLaneIdEmptyTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    Assertions.assertThat(routeWithLocation.getOffsetLaneId(elementCount).isEmpty()).isTrue();
  }
}
