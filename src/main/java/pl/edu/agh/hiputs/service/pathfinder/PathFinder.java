package pl.edu.agh.hiputs.service.pathfinder;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.util.Pair;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

import java.util.List;
import java.util.concurrent.Executor;

public interface PathFinder<V extends JunctionReadable, E extends LaneReadable> {

    /**
     * Find shortest path from source to sink
     * @param request Pair containing source and sink vertex
     * @return path from source to sink
     */
    GraphPath<V, E> getPath(Pair<V, V> request);

    /**
     * Finds multiple paths from source to sink
     * @param requests list of pairs containing source and sink vertex
     * @return list of paths from source to sink
     */
    List<GraphPath<V, E>> getPaths(List<Pair<V, V>> requests);

    /**
     * Parallel implementation of getPaths method
     * @param requests list of pairs containing source and sink vertex
     * @param executor method executors
     * @return list of paths from source to sink
     */
    List<GraphPath<V, E>> getPathsWithExecutor(List<Pair<V, V>> requests, Executor executor);

    /**
     * Finds multiple paths from source to random sinks
     * @param starts list of source vertexes
     * @return list of paths from source to sink
     */
    List<Pair<V, GraphPath<V, E>>> getPathsToRandomSink(List<V> starts);

    /**
     * Parallel implementation of getPathsToRandomSink method
     * @param starts list of source vertexes
     * @param executor method executors
     * @return list of sink vertex and paths from source to sink
     */
    List<Pair<V, GraphPath<V, E>>> getPathsToRandomSinkWithExecutor(List<V> starts, Executor executor);

    /**
     * Create n random paths
     * @param n number of requests
     * @return list of sources and sinks and path between them
     */
    List<Pair<Pair<V,V>, GraphPath<V, E>>> getRandomPaths(int n);

    /**
     * Parallel implementation of getRandomPaths method
     * @param n number of requests
     * @param executor method executors
     * @return list of sink vertex and paths from source to sink
     */
    List<Pair<Pair<V,V>, GraphPath<V, E>>> getRandomPathsWithExecutor(int n, Executor executor);
}
