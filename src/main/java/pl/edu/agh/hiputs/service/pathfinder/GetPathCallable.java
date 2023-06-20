package pl.edu.agh.hiputs.service.pathfinder;

import org.jgrapht.alg.util.Pair;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.LaneId;

import java.util.concurrent.Callable;

public class GetPathCallable implements Callable<RouteWithLocation> {
    Pair<LaneId, LaneId> request;
    CHPathFinder pathFinder;

    public GetPathCallable(Pair<LaneId, LaneId> request, CHPathFinder pathFinder) {
        this.request = request;
        this.pathFinder = pathFinder;
    }

    public RouteWithLocation call() {
        return pathFinder.getPath(request);
    }
}
