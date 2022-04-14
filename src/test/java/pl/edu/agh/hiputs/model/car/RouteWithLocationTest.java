package pl.edu.agh.hiputs.model.car;

import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;

public class RouteWithLocationTest {

  private static final List<JunctionId> orderedJunctionsOnRoute = new ArrayList<>();
  private static final List<LaneId> orderedLaneIdsOnRoute = new ArrayList<>();
  private static List<RouteElement> routeElements;

  @BeforeAll
  public static void setup() {
    routeElements = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
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
    Assertions.assertThat(routeWithLocation.getNextRouteElement())
        .isEqualTo(new RouteElement(orderedJunctionsOnRoute.get(0), orderedLaneIdsOnRoute.get(0)));
  }

  @Test
  public void getNextJunctionIdTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    Assertions.assertThat(routeWithLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(0));
  }

  @Test
  public void getNextLaneIdTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    Assertions.assertThat(routeWithLocation.getNextLaneId()).isEqualTo(orderedLaneIdsOnRoute.get(0));
  }

  @Test
  public void moveOneForwardTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    routeWithLocation.moveOneForward();
    Assertions.assertThat(routeWithLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(1));
    Assertions.assertThat(routeWithLocation.getNextLaneId()).isEqualTo(orderedLaneIdsOnRoute.get(1));
  }

  @Test
  public void traversWholeRouteByOneMoveForwardOnlyTest() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    for (int i = 1; i < 5; i++) {
      routeWithLocation.moveOneForward();
      Assertions.assertThat(routeWithLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(i));
      Assertions.assertThat(routeWithLocation.getNextLaneId()).isEqualTo(orderedLaneIdsOnRoute.get(i));
    }
    Assertions.assertThatThrownBy(routeWithLocation::moveOneForward);
  }

  @Test
  public void setCurrentPosition() {
    RouteWithLocation routeWithLocation = new RouteWithLocation(routeElements, 0);
    routeWithLocation.setCurrentPosition(4);
    Assertions.assertThat(routeWithLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(4));
    Assertions.assertThat(routeWithLocation.getNextLaneId()).isEqualTo(orderedLaneIdsOnRoute.get(4));
  }
}
