package pl.edu.agh.hiputs.service.pathfinder;


import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class CHPathFinder implements PathFinder<LaneId> {

    final Map<LaneId, Pair<JunctionReadable, JunctionReadable>> laneToJunctionsMapping = new HashMap<>();
    final ArrayList<LaneId> laneIds = new ArrayList<>();
    ShortestPathAlgorithm<JunctionReadable, LaneReadable> shortestPathAlgorithm;

    public CHPathFinder(MapRepository mapRepository, ThreadPoolExecutor executor) {
        Graph<JunctionReadable, LaneReadable> graph = createGraphFromMapRepository(mapRepository);
        shortestPathAlgorithm = initShortestPathAlgorithm(graph, executor);
        laneIds.addAll(laneToJunctionsMapping.keySet());
    }

    public CHPathFinder(MapFragment mapFragment, ThreadPoolExecutor executor) {
        Graph<JunctionReadable, LaneReadable> graph = createGraphFromMapFragment(mapFragment);
        shortestPathAlgorithm = initShortestPathAlgorithm(graph, executor);
        laneIds.addAll(laneToJunctionsMapping.keySet());
    }

    Graph<JunctionReadable, LaneReadable> createGraphFromMapRepository(MapRepository mapRepository) {
        Graph<JunctionReadable, LaneReadable> graph = new SimpleDirectedWeightedGraph<>(LaneReadable.class);
        List<Patch> patches = mapRepository.getAllPatches();
        for (Patch patch: patches) {
            addPatchDataToGraph(patch, graph);
        }
        return graph;
    }

    Graph<JunctionReadable, LaneReadable> createGraphFromMapFragment(MapFragment mapFragment) {
        Graph<JunctionReadable, LaneReadable> graph = new SimpleDirectedWeightedGraph<>(LaneReadable.class);
        Set<PatchReader> patches = mapFragment.getKnownPatchReadable();
        for (PatchReader patch: patches) {
            addPatchDataToGraph(patch, graph);
        }
        return graph;
    }

    void addPatchDataToGraph(PatchReader patch, Graph<JunctionReadable, LaneReadable> graph) {
        patch.getLaneIds();
        for (LaneId laneId: patch.getLaneIds()) {
            LaneReadable laneReadable = patch.getLaneReadable(laneId);

            JunctionReadable incomingJunction = patch.getJunctionReadable(laneReadable.getIncomingJunctionId());
            JunctionReadable outgoingJunction = patch.getJunctionReadable(laneReadable.getOutgoingJunctionId());
            laneToJunctionsMapping.put(laneId, new Pair<>(incomingJunction, outgoingJunction));
            if (!graph.containsVertex(incomingJunction)) {
                graph.addVertex(incomingJunction);
            }
            if (!graph.containsVertex(outgoingJunction)) {
                graph.addVertex(outgoingJunction);
            }
            if (!graph.containsEdge(laneReadable)) {
                graph.addEdge(incomingJunction, outgoingJunction, laneReadable);
                graph.setEdgeWeight(laneReadable, laneReadable.getLength());
            }
        }
    }

    abstract ShortestPathAlgorithm<JunctionReadable, LaneReadable> initShortestPathAlgorithm(
            Graph<JunctionReadable, LaneReadable> graph,
            ThreadPoolExecutor executor);

    @Override
    public List<RouteWithLocation> getPaths(List<Pair<LaneId, LaneId>> requests) {
        System.out.println("In getPath");
        List<RouteWithLocation> routeWithLocationList = new ArrayList<>();
        for (Pair<LaneId, LaneId> request: requests) {
            routeWithLocationList.add(getPath(request));
        }
        return routeWithLocationList;
    }

    @Override
    public List<RouteWithLocation> getPathsWithExecutor(List<Pair<LaneId, LaneId>> requests, ThreadPoolExecutor executor) {
        System.out.println("In getPath executors");
        List<RouteWithLocation> routeWithLocationList = new ArrayList<>();
        List<Future<RouteWithLocation>> futureRoutesWithLocation = new ArrayList<>();
        for (Pair<LaneId, LaneId> request: requests) {
            GetPathCallable callable = new GetPathCallable(request, this);
            futureRoutesWithLocation.add(executor.submit(callable));
        }
        try {
            for (int i = 0; i < requests.size(); i++) {
                routeWithLocationList.add(futureRoutesWithLocation.get(i).get());
            }
        }
        catch (Exception e) {
            System.out.println("Cannot collect future\n" + e);
        }
        return routeWithLocationList;
    }

    @Override
    public List<Pair<LaneId, RouteWithLocation>> getPathsToRandomSink(List<LaneId> starts) {
        List<Pair<LaneId, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
        for (LaneId start: starts) {
            LaneId end = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
            routeWithLocationList.add(new Pair<>(end, getPath(new Pair<>(start, end))));
        }
        return routeWithLocationList;
    }

    @Override
    public List<Pair<LaneId, RouteWithLocation>> getPathsToRandomSinkWithExecutor(List<LaneId> starts, ThreadPoolExecutor executor) {
        List<Pair<LaneId, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
        List<Future<RouteWithLocation>> futureRoutesWithLocation = new ArrayList<>();
        List<LaneId> ends = new ArrayList<>();
        for (LaneId start: starts) {
            LaneId end = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
            ends.add(end);
            GetPathCallable callable = new GetPathCallable(new Pair<>(start, end), this);
            futureRoutesWithLocation.add(executor.submit(callable));
        }
        try {
            for (int i = 0; i < starts.size(); i++) {
                routeWithLocationList.add(new Pair<>(ends.get(i), futureRoutesWithLocation.get(i).get()));
            }
        }
        catch (Exception e) {
            System.out.println("Cannot collect future\n" + e);
        }
        return routeWithLocationList;
    }

    @Override
    public List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> getRandomPaths(int n) {
        List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
        for (int i=0; i<n; i++) {
            LaneId start = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
            LaneId end = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
            routeWithLocationList.add(new Pair<>(new Pair<>(start, end), getPath(new Pair<>(start, end))));
        }
        return routeWithLocationList;
    }

    @Override
    public List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> getRandomPathsWithExecutor(int n, ThreadPoolExecutor executor) {
        List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> routeWithLocationList = new ArrayList<>();
        List<Future<RouteWithLocation>> futureRoutesWithLocation = new ArrayList<>();
        List<Pair<LaneId, LaneId>> requests = new ArrayList<>();
        for (int i=0; i<n; i++) {
            LaneId start = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
            LaneId end = laneIds.get(ThreadLocalRandom.current().nextInt(laneIds.size()));
            requests.add(new Pair<>(start, end));
            GetPathCallable callable = new GetPathCallable(new Pair<>(start, end), this);
            futureRoutesWithLocation.add(executor.submit(callable));
        }
        try {
            for (int i=0; i<n; i++) {
                routeWithLocationList.add(new Pair<>(requests.get(i), futureRoutesWithLocation.get(i).get()));
            }
        }
        catch (Exception e) {
            System.out.println("Cannot collect future\n" + e);
        }
        return routeWithLocationList;
    }

    @Override
    public RouteWithLocation getPath(Pair<LaneId, LaneId> request) {
        LinkedList<RouteElement> routeElements = new LinkedList<>();
        if (request.getFirst().equals(request.getSecond())) {
            routeElements.addLast(
                    new RouteElement(
                            laneToJunctionsMapping.get(request.getFirst()).getFirst().getJunctionId(),
                            request.getFirst()
                    )
            );
            return new RouteWithLocation(routeElements, 0);
        }
        JunctionReadable incomingJunction = laneToJunctionsMapping.get(request.getFirst()).getSecond();
        JunctionReadable outgoingJunction = laneToJunctionsMapping.get(request.getSecond()).getFirst();


        if (!incomingJunction.getJunctionId().equals(outgoingJunction.getJunctionId())) {
            GraphPath<JunctionReadable, LaneReadable> graphPath = shortestPathAlgorithm.getPath(incomingJunction, outgoingJunction);

            if (graphPath != null) {
                int size = graphPath.getEdgeList().size();
                for (int i=0; i<size; i++) {
                    routeElements.addLast(new RouteElement(
                            graphPath.getVertexList().get(i).getJunctionId(),
                            graphPath.getEdgeList().get(i).getLaneId()));
                }
            }
        }

        routeElements.addFirst(
                new RouteElement(
                        laneToJunctionsMapping.get(request.getFirst()).getFirst().getJunctionId(),
                        request.getFirst()
                )
        );
        routeElements.addLast(
                new RouteElement(
                        outgoingJunction.getJunctionId(),
                        request.getSecond()
                )
        );

        return new RouteWithLocation(routeElements, 0);
    }
}
