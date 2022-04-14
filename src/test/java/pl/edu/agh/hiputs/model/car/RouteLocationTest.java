package pl.edu.agh.hiputs.model.car;

import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;

public class RouteLocationTest {

  private static final List<JunctionId> orderedJunctionsOnRoute = new ArrayList<>();
  private static final List<LaneId> orderedLanesOnRoute = new ArrayList<>();
  private static List<RouteElement> routeElements;

  @BeforeAll
  public static void setup() {
    routeElements = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      JunctionId junctionId = new JunctionId(String.valueOf(i), JunctionType.BEND);
      LaneId laneId = new LaneId(String.valueOf(i));
      orderedJunctionsOnRoute.add(junctionId);
      orderedLanesOnRoute.add(laneId);
      routeElements.add(new RouteElement(junctionId, laneId));
    }
  }

  @Test
  public void getNextRouteElementTest() {
    RouteLocation routeLocation = new RouteLocation(routeElements, 0);
    Assertions.assertThat(routeLocation.getNextRouteElement())
        .isEqualTo(new RouteElement(orderedJunctionsOnRoute.get(0), orderedLanesOnRoute.get(0)));
  }

  @Test
  public void getNextJunctionIdTest() {
    RouteLocation routeLocation = new RouteLocation(routeElements, 0);
    Assertions.assertThat(routeLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(0));
  }

  @Test
  public void getNextLaneIdTest() {
    RouteLocation routeLocation = new RouteLocation(routeElements, 0);
    Assertions.assertThat(routeLocation.getNextLaneId()).isEqualTo(orderedLanesOnRoute.get(0));
  }

  @Test
  public void moveOneForwardTest() {
    RouteLocation routeLocation = new RouteLocation(routeElements, 0);
    routeLocation.moveOneForward();
    Assertions.assertThat(routeLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(1));
    Assertions.assertThat(routeLocation.getNextLaneId()).isEqualTo(orderedLanesOnRoute.get(1));
  }

  @Test
  public void traversWholeRouteByOneMoveForwardOnlyTest() {
    RouteLocation routeLocation = new RouteLocation(routeElements, 0);
    for (int i = 1; i < 5; i++) {
      routeLocation.moveOneForward();
      Assertions.assertThat(routeLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(i));
      Assertions.assertThat(routeLocation.getNextLaneId()).isEqualTo(orderedLanesOnRoute.get(i));
    }
    Assertions.assertThatThrownBy(routeLocation::moveOneForward);
  }

  @Test
  public void setCurrentPosition() {
    RouteLocation routeLocation = new RouteLocation(routeElements, 0);
    routeLocation.setCurrentPosition(4);
    Assertions.assertThat(routeLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(4));
    Assertions.assertThat(routeLocation.getNextLaneId()).isEqualTo(orderedLanesOnRoute.get(4));
  }
}
