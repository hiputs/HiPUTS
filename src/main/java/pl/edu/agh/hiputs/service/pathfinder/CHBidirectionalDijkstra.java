package pl.edu.agh.hiputs.service.pathfinder;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.ContractionHierarchyBidirectionalDijkstra;
import org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


public class CHBidirectionalDijkstra implements PathFinder<JunctionReadable, LaneReadable> {

    ContractionHierarchyBidirectionalDijkstra<JunctionReadable, LaneReadable> dijkstraShortestPath;

    public CHBidirectionalDijkstra(MapFragment fragment, ThreadPoolExecutor executor) {
        Graph<JunctionReadable, LaneReadable> graph = new SimpleDirectedWeightedGraph<>(LaneReadable.class);
        Set<PatchReader> patches = fragment.getKnownPatchReadable();
        for (PatchReader patch: patches) {
            patch.getLaneIds();
            for (LaneId laneId: patch.getLaneIds()) {
                LaneReadable laneReadable = fragment.getLaneReadable(laneId);
                JunctionReadable incomingJunction = fragment.getJunctionReadable(laneReadable.getIncomingJunctionId());
                JunctionReadable outgoingJunction = fragment.getJunctionReadable(laneReadable.getOutgoingJunctionId());
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

        ContractionHierarchyPrecomputation<JunctionReadable, LaneReadable> precomputation =
                new ContractionHierarchyPrecomputation<>(graph, executor);

        ContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable> ch =
                precomputation.computeContractionHierarchy();


        dijkstraShortestPath = new ContractionHierarchyBidirectionalDijkstra<>(ch);
    }

    private GraphPath<JunctionReadable, LaneReadable> findRoute(JunctionReadable source, JunctionReadable sink) {
        return dijkstraShortestPath.getPath(source, sink);
    }


    @Override
    public GraphPath<JunctionReadable, LaneReadable> getPath(Pair<JunctionReadable, JunctionReadable> request) {
        return null;
    }

    @Override
    public List<GraphPath<JunctionReadable, LaneReadable>> getPaths(List<Pair<JunctionReadable, JunctionReadable>> requests) {
        return null;
    }

    @Override
    public List<GraphPath<JunctionReadable, LaneReadable>> getPathsWithExecutor(List<Pair<JunctionReadable, JunctionReadable>> requests, Executor executor) {
        return null;
    }

    @Override
    public List<Pair<JunctionReadable, GraphPath<JunctionReadable, LaneReadable>>> getPathsToRandomSink(List<JunctionReadable> starts) {
        return null;
    }

    @Override
    public List<Pair<JunctionReadable, GraphPath<JunctionReadable, LaneReadable>>> getPathsToRandomSinkWithExecutor(List<JunctionReadable> starts, Executor executor) {
        return null;
    }

    @Override
    public List<Pair<Pair<JunctionReadable, JunctionReadable>, GraphPath<JunctionReadable, LaneReadable>>> getRandomPaths(int n) {
        return null;
    }

    @Override
    public List<Pair<Pair<JunctionReadable, JunctionReadable>, GraphPath<JunctionReadable, LaneReadable>>> getRandomPathsWithExecutor(int n, Executor executor) {
        return null;
    }
}
