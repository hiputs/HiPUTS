package pl.edu.agh.hiputs.service.routegenerator.generator.pathfinder;

import org.jgrapht.graph.GraphWalk;

import java.util.List;

/**
 * Interface to finds shortest path in graph with default algorithms,
 * each class implementing this interface should be different algorithm of finding paths.
 * Implementations should have a possibility to parallelize computation for large amount of requests.
 * @param <V> - graph Vertex
 * @param <E> - graph Edge
 */
public interface PathFinder<V, E> {
  /**
   * Finds the shortest path from source to sink.
   * @param source - starting vertex
   * @param sink - ending vertex
   * @return Shortest path from source to sink
   */
  GraphWalk<V, E> findPath(V source, V sink);

  /**
   * Method is preferred to use instead for multiple findPath shorten execution time thanks to multiprocessing multiprocessing
   * @param sourcesList - list of starting vertexes
   * @param sinksLink - list of ending vertexes
   * @return List of shortest path, first element contains a shortest path between firsts elements of sourcesList and sinksList
   */
  List<GraphWalk<V, E>> findPaths(List<V> sourcesList, List<V> sinksLink);
}
