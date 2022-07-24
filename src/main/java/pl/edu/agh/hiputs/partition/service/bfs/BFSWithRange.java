package pl.edu.agh.hiputs.partition.service.bfs;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
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
    Set<Edge<T, S>> resultSet = new HashSet<>();
    Set<Node<T, S>> borderNodes = new HashSet<>();
    Queue<Node<T, S>> front = new LinkedList<>();
    root.setDistance(0.0);
    front.add(root);
    while (!front.isEmpty()) {
      Node<T, S> currentNode = front.poll();

      if (currentNode.getDistance() > range) {
        borderNodes.add(currentNode);
        continue;
      }

      currentNode.getOutgoingEdges().forEach(e -> {
        resultSet.add(e);

        Node<T, S> nextNode = e.getTarget();
        double currentDistance = currentNode.getDistance() + measure.measure(e);
        if (nextNode.getDistance() == null || nextNode.getDistance() > currentDistance) {
          nextNode.setDistance(currentDistance);
          front.add(nextNode);
        }
      });
    }

    //cleanup
    resultSet.stream().flatMap(e -> Stream.of(e.getSource(), e.getTarget())).forEach(n -> n.setDistance(null));

    return BFSWithRangeResult.<T, S>builder()
        .edgesInRange(resultSet)
        .borderNodes(borderNodes)
        .build();
  }

}
