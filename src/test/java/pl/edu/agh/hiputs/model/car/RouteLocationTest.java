package pl.edu.agh.hiputs.model.car;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.ArrayList;
import java.util.List;

public class RouteLocationTest {

    private static Route route;
    private static List<JunctionId> orderedJunctionsOnRoute = new ArrayList<>();
    private static List<LaneId> orderedLanesOnRoute = new ArrayList<>();

    @BeforeAll
    public static void setup() {
        ArrayList<RouteElement> routeElements = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            JunctionId junctionId = new JunctionId(String.valueOf(i), JunctionType.BEND);
            LaneId laneId = new LaneId(String.valueOf(i));
            orderedJunctionsOnRoute.add(junctionId);
            orderedLanesOnRoute.add(laneId);
            routeElements.add(new RouteElement(junctionId, laneId));
        }
        route = new Route(routeElements);
    }

    @Test
    public void getNextRouteElementTest() {
        RouteLocation routeLocation = new RouteLocation(route);
        Assertions.assertThat(routeLocation.getNextRouteElement())
                .isEqualTo(new RouteElement(orderedJunctionsOnRoute.get(0), orderedLanesOnRoute.get(0)));
    }

    @Test
    public void getNextJunctionIdTest() {
        RouteLocation routeLocation = new RouteLocation(route);
        Assertions.assertThat(routeLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(0));
    }

    @Test
    public void getNextLaneIdTest() {
        RouteLocation routeLocation = new RouteLocation(route);
        Assertions.assertThat(routeLocation.getNextLaneId()).isEqualTo(orderedLanesOnRoute.get(0));
    }

    @Test
    public void moveOneForwardTest() {
        RouteLocation routeLocation = new RouteLocation(route);
        routeLocation.moveOneForward();
        Assertions.assertThat(routeLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(1));
        Assertions.assertThat(routeLocation.getNextLaneId()).isEqualTo(orderedLanesOnRoute.get(1));
    }

    @Test
    public void traversWholeRouteByOneMoveForwardOnlyTest() {
        RouteLocation routeLocation = new RouteLocation(route);
        for (int i = 1; i < 5; i++) {
            routeLocation.moveOneForward();
            Assertions.assertThat(routeLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(i));
            Assertions.assertThat(routeLocation.getNextLaneId()).isEqualTo(orderedLanesOnRoute.get(i));
        }
        Assertions.assertThatThrownBy(routeLocation::moveOneForward);
    }

    @Test
    public void setCurrentPosition() {
        RouteLocation routeLocation = new RouteLocation(route);
        routeLocation.setCurrentPosition(4);
        Assertions.assertThat(routeLocation.getNextJunctionId()).isEqualTo(orderedJunctionsOnRoute.get(4));
        Assertions.assertThat(routeLocation.getNextLaneId()).isEqualTo(orderedLanesOnRoute.get(4));
    }
}
