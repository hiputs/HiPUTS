package pl.edu.agh.hiputs.service.routegenerator.generator.pathfinder;

import org.jgrapht.graph.GraphWalk;

import java.util.List;

public class SimplePathFinder<V,E> implements PathFinder{

  @Override
  public GraphWalk<V, E> findPath(Object source, Object sink) {
    return null;
  }

  @Override
  public List<GraphWalk<V, E>> findPaths(List sourcesList, List sinksLink) {
    return null;
  }
}
