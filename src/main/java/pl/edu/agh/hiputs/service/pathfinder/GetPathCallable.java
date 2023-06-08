package pl.edu.agh.hiputs.service.pathfinder;

import org.jgrapht.alg.util.Pair;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.concurrent.Callable;

public class GetPathCallable implements Callable<RouteWithLocation> {
    Pair<LaneId, LaneId> request;
    CHPathFinder pathFinder;

    public GetPathCallable(Pair<LaneId, LaneId> request, CHPathFinder chBidirectionalDijkstra) {
        this.request = request;
        this.pathFinder = chBidirectionalDijkstra;
    }

    public RouteWithLocation call() {
        return pathFinder.getPath(request);
    }
}
