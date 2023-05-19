package pl.edu.agh.hiputs.service.pathfinder;

import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.ContractionHierarchyBidirectionalDijkstra;
import org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.*;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class CHBidirectionalDijkstra implements PathFinder<LaneId> {
    private ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable> ch;
    private ContractionHierarchyBidirectionalDijkstra<JunctionReadable, LaneReadable> dijkstraShortestPath;
    private final Map<LaneId, Pair<JunctionReadable, JunctionReadable>> laneToJunctionsMapping = new HashMap<>();

    public CHBidirectionalDijkstra(MapRepository mapRepository, ThreadPoolExecutor executor) {
        Graph<JunctionReadable, LaneReadable> graph = createGraphFromMapRepository(mapRepository);
        ch = createCHBidirectionalDijkstra(graph, executor);
        dijkstraShortestPath = new ContractionHierarchyBidirectionalDijkstra<>(ch);
    }

    public CHBidirectionalDijkstra(String precomputationFilePath) {
        try {
            ch = readObjectFromFile(precomputationFilePath);
            dijkstraShortestPath = new ContractionHierarchyBidirectionalDijkstra<>(ch);
        }
        catch(Exception e) {
            log.error("Cannot read CHBidirectionalDijkstra precomputation:" + e);
        }
    }

    private Graph<JunctionReadable, LaneReadable> createGraphFromMapRepository(MapRepository mapRepository) {
        Graph<JunctionReadable, LaneReadable> graph = new SimpleDirectedWeightedGraph<>(LaneReadable.class);
        List<Patch> patches = mapRepository.getAllPatches();
        for (PatchReader patch: patches) {
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
                if (graph.containsEdge(laneReadable)) {
                    graph.addEdge(incomingJunction, outgoingJunction, laneReadable);
                    graph.setEdgeWeight(laneReadable, laneReadable.getLength());
                }
            }
        }
        return graph;
    }

    private ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable> createCHBidirectionalDijkstra(
            Graph<JunctionReadable, LaneReadable> graph,
            ThreadPoolExecutor executor) {
        ContractionHierarchyPrecomputation<JunctionReadable, LaneReadable> precomputation =
                new ContractionHierarchyPrecomputation<>(graph, executor);

        return precomputation.computeContractionHierarchy();
    }

    // Serialization
    // Save object into a file.
    public void dumpPrecomputationToFile(String filePath) throws IOException {
        File file = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(dijkstraShortestPath);
            oos.writeObject(ch.getContractionMapping());
            oos.flush();
        }
    }

    // Deserialization
    // Get object from a file.
    public static ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable>  readObjectFromFile(String filePath) throws IOException, ClassNotFoundException {
        File file = new File(filePath);
        ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable>  result;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            result = (ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable>) ois.readObject();

        }
        Graph<ContractionHierarchyPrecomputation.ContractionVertex<JunctionReadable>, ContractionHierarchyPrecomputation.ContractionEdge<LaneReadable>> graph = null;
        return result;
    }

    private GraphPath<JunctionReadable, LaneReadable> findRoute(JunctionReadable source, JunctionReadable sink) {
        return dijkstraShortestPath.getPath(source, sink);
    }

    @Override
    public RouteWithLocation getPath(Pair<LaneId, LaneId> request) {
        JunctionReadable incomingJunction = laneToJunctionsMapping.get(request.getFirst()).getFirst();
        JunctionReadable outgoingJunction = laneToJunctionsMapping.get(request.getSecond()).getSecond();
        GraphPath<JunctionReadable, LaneReadable> graphPath = findRoute(incomingJunction, outgoingJunction);
        LinkedList<RouteElement> routeElements = new LinkedList<>();
        int size = graphPath.getEdgeList().size();
        for (int i=0; i<size; i++) {
            routeElements.addLast(new RouteElement(
                    graphPath.getVertexList().get(i).getJunctionId(),
                    graphPath.getEdgeList().get(i).getLaneId()));
        }

        return new RouteWithLocation(routeElements, 0);
    }

    @Override
    public List<RouteWithLocation> getPaths(List<Pair<LaneId, LaneId>> requests) {
        return null;
    }

    @Override
    public List<RouteWithLocation> getPathsWithExecutor(List<Pair<LaneId, LaneId>> requests, Executor executor) {
        return null;
    }

    @Override
    public List<Pair<LaneId, RouteWithLocation>> getPathsToRandomSink(List<LaneId> starts) {
        return null;
    }

    @Override
    public List<Pair<LaneId, RouteWithLocation>> getPathsToRandomSinkWithExecutor(List<LaneId> starts, Executor executor) {
        return null;
    }

    @Override
    public List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> getRandomPaths(int n) {
        return null;
    }

    @Override
    public List<Pair<Pair<LaneId, LaneId>, RouteWithLocation>> getRandomPathsWithExecutor(int n, Executor executor) {
        return null;
    }
}
