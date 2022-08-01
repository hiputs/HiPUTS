package pl.edu.agh.hiputs.partition.service.bfs;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.EdgeData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.graph.NodeData;

@RequiredArgsConstructor
public class BFSWithRange<T extends NodeData, S extends EdgeData> {

  private final Double range;
  private final Measure<Edge<T, S>> measure;

  public BFSWithRangeResult<T, S> getInRange(Graph<T, S> graph, Node<T, S> root) {
    return getInRange(graph, root, null, false);
  }

  public BFSWithRangeResult<T, S> getInRange(Graph<T, S> graph, Node<T, S> root, Set<Edge<T,S>> edgesToMoveOn, boolean removePartiallyReachableEdges) {
    List<Edge<T, S>> resultSet = new LinkedList<>();
    Queue<Node<T, S>> front = new LinkedList<>();
    root.setDistance(0.0);
    front.add(root);
    while (!front.isEmpty()) {
      Node<T, S> currentNode = front.poll();

      if (currentNode.getDistance() > range) {
        continue;
      }

      currentNode.getOutgoingEdges().forEach(e -> {
        if(edgesToMoveOn != null && !edgesToMoveOn.stream().map(Edge::getId).collect(Collectors.toSet()).contains(e.getId())) {
          return;
        }
        resultSet.add(e);

        Node<T, S> nextNode = e.getTarget();
        double currentDistance = currentNode.getDistance() + measure.measure(e);
        if (nextNode.getDistance() == null || nextNode.getDistance() > currentDistance) {
          nextNode.setDistance(currentDistance);
          front.add(nextNode);
        }
      });
    }

    Set<Node<T, S>> borderNodes = resultSet.stream()
        .flatMap(e -> Stream.of(e.getSource(), e.getTarget()))
        .filter(n -> n.getDistance() > range)
        .collect(
        Collectors.toSet());

    //cleanup
    resultSet.stream().flatMap(e -> Stream.of(e.getSource(), e.getTarget())).forEach(n -> n.setDistance(null));
    List<Edge<T, S>> edgesInRange = resultSet;
    if (removePartiallyReachableEdges) {
      edgesInRange = resultSet.stream().filter(e -> !borderNodes.contains(e.getTarget())).collect(Collectors.toList());
    }

    return BFSWithRangeResult.<T, S>builder()
        .edgesInRange(edgesInRange)
        .borderNodes(borderNodes)
        .build();
  }

}
