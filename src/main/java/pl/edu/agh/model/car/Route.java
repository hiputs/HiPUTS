package pl.edu.agh.model.car;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Route {

    /**
     * List of RouteElements - effectively pairs of (junctionId, laneId) witch precisely indicates
     * what are consecutive object witch should be visited by car. Information where to go on next junction
     * could be simply obtained by checking actual position (laneId od junctionId) and finding it on route.
     * In order to reduce complexity of that kind of search, information of position on route is cached
     * and maintained in RouteLocation object situated in vehicle.
     */
    private List<RouteElement> routeElements;
}
