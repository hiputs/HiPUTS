package pl.edu.agh.hiputs.service.pathfinder;

import org.jgrapht.alg.util.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.example.ExampleMapFragmentProvider;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;


import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
public class CHBidirectionalAStarTest {

    void comparePathWithRequest(MapFragment mapFragment, RouteWithLocation route, LaneId source, LaneId sink) {
        // checking start and end
        assertEquals(source, route.getRouteElements().get(0).getOutgoingLaneId());
        assertEquals(sink, route.getRouteElements().get(route.getRouteElements().size()-1).getOutgoingLaneId());

        //checking if route is correct
        RouteElement prevRouteElement = null;
        for (RouteElement routeElement: route.getRouteElements()) {
            if (prevRouteElement != null) {
                JunctionReadable junction = mapFragment.getJunctionReadable(routeElement.getJunctionId());
                JunctionReadable prevJunction = mapFragment.getJunctionReadable(prevRouteElement.getJunctionId());
                LaneReadable lane = mapFragment.getLaneReadable(prevRouteElement.getOutgoingLaneId());
                assertEquals(lane.getIncomingJunctionId(), prevJunction.getJunctionId());
                assertEquals(lane.getOutgoingJunctionId(), junction.getJunctionId());
            }
            prevRouteElement = routeElement;
        }
    }

    @Test
    void getPathCHDijkstra() {
        MapFragment mapFragment = ExampleMapFragmentProvider.getCircleMapWithCrossroad();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        CHBidirectionalAStar bidirectionalAStar = new CHBidirectionalAStar(mapFragment, executor);

        List<LaneId> list = mapFragment.getLocalLaneIds().stream().toList();
        RouteWithLocation route = bidirectionalAStar.getPath(new Pair<>(list.get(1), list.get(6)));

        comparePathWithRequest(mapFragment, route, list.get(1), list.get(6));
    }

    @Test
    void processRequests() {
        MapFragment mapFragment = ExampleMapFragmentProvider.getCircleMapWithCrossroad();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        CHBidirectionalAStar bidirectionalAStar = new CHBidirectionalAStar(mapFragment, executor);
        List<LaneId> list = mapFragment.getLocalLaneIds().stream().toList();
        List<Pair<LaneId, LaneId>> requests = List.of(
                new Pair<>(list.get(1), list.get(6)),
                new Pair<>(list.get(0), list.get(4))
        );
        List<RouteWithLocation> routes = bidirectionalAStar.getPaths(requests);
        for (int i=0; i<routes.size(); i++) {
            comparePathWithRequest(mapFragment, routes.get(i), requests.get(i).getFirst(), requests.get(i).getSecond());
        }
    }

    @Test
    void processRequestsWithExecutors() {
        MapFragment mapFragment = ExampleMapFragmentProvider.getCircleMapWithCrossroad();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        CHBidirectionalAStar bidirectionalAStar = new CHBidirectionalAStar(mapFragment, executor);
        List<LaneId> list = mapFragment.getLocalLaneIds().stream().toList();
        List<Pair<LaneId, LaneId>> requests = List.of(
                new Pair<>(list.get(1), list.get(6)),
                new Pair<>(list.get(0), list.get(4))
        );
        List<RouteWithLocation> routes = bidirectionalAStar.getPathsWithExecutor(requests, executor);
        for (int i=0; i<routes.size(); i++) {
            comparePathWithRequest(mapFragment, routes.get(i), requests.get(i).getFirst(), requests.get(i).getSecond());
        }
    }

    @Test
    void processRequestsToRandomLocations() {
        MapFragment mapFragment = ExampleMapFragmentProvider.getCircleMapWithCrossroad();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        CHBidirectionalAStar bidirectionalAStar = new CHBidirectionalAStar(mapFragment, executor);
        List<LaneId> list = mapFragment.getLocalLaneIds().stream().toList();
        List<LaneId > requests = List.of(
                list.get(1),
                list.get(0)
        );
        List<Pair<LaneId, RouteWithLocation>> routesAndStarts = bidirectionalAStar.getPathsToRandomSink(requests);
        for (int i=0; i<routesAndStarts.size(); i++) {
            comparePathWithRequest(mapFragment, routesAndStarts.get(i).getSecond(), requests.get(i), routesAndStarts.get(i).getFirst());
        }
    }

    @Test
    void processRequestsToRandomLocationsWithExecutors() {
        MapFragment mapFragment = ExampleMapFragmentProvider.getCircleMapWithCrossroad();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        CHBidirectionalAStar bidirectionalAStar = new CHBidirectionalAStar(mapFragment, executor);
        List<LaneId> list = mapFragment.getLocalLaneIds().stream().toList();
        List<LaneId > requests = List.of(
                list.get(1),
                list.get(0)
        );
        List<Pair<LaneId, RouteWithLocation>> routesAndStarts = bidirectionalAStar.getPathsToRandomSinkWithExecutor(requests, executor);
        for (int i=0; i<routesAndStarts.size(); i++) {
            comparePathWithRequest(mapFragment, routesAndStarts.get(i).getSecond(), requests.get(i), routesAndStarts.get(i).getFirst());
        }
    }

    @Test
    void processRandomRequests() {
        MapFragment mapFragment = ExampleMapFragmentProvider.getCircleMapWithCrossroad();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        CHBidirectionalAStar bidirectionalAStar = new CHBidirectionalAStar(mapFragment, executor);

        List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> routesAndStarts = bidirectionalAStar.getRandomPaths(2);
        for (Pair<Pair<LaneId, LaneId>, RouteWithLocation> routesAndStart : routesAndStarts) {
            comparePathWithRequest(
                    mapFragment,
                    routesAndStart.getSecond(),
                    routesAndStart.getFirst().getFirst(),
                    routesAndStart.getFirst().getSecond());
        }
    }

    @Test
    void processRandomRequestsWithExecutors() {
        MapFragment mapFragment = ExampleMapFragmentProvider.getCircleMapWithCrossroad();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        CHBidirectionalAStar bidirectionalAStar = new CHBidirectionalAStar(mapFragment, executor);

        List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> routesAndStarts = bidirectionalAStar.getRandomPathsWithExecutor(2, executor);
        for (Pair<Pair<LaneId, LaneId>, RouteWithLocation> routesAndStart : routesAndStarts) {
            comparePathWithRequest(
                    mapFragment,
                    routesAndStart.getSecond(),
                    routesAndStart.getFirst().getFirst(),
                    routesAndStart.getFirst().getSecond());
        }
    }

    @Test
    void serializing() {
    }

    @Test
    void serializingAndDeserializing() {

    }

    @Test
    void serializationCorrectness() {

    }
}
