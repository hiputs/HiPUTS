package pl.edu.agh.hiputs.server.partition.model.graph;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class Graph<T extends NodeData, S extends EdgeData> {

    private final Map<String, Node<T, S>> nodes = new HashMap<>();
    private final Map<String, Edge<T, S>> edges = new HashMap<>();

    public void addNode(Node<T, S> newNode) {
      if (!edges.isEmpty()) {
        throw new RuntimeException("Cannot add new node when graph contains edges");
      }

      String newNodeId = newNode.getId();
      if(nodes.containsKey(newNodeId)) {
        nodes.get(newNodeId).merge(newNode);
      } else {
        nodes.put(newNodeId, newNode);
      }
    }

    public void addEdge(Edge<T, S> edge) {
      Node<T, S> source = nodes.get(edge.getSource().getId());
      edge.setSource(source);
      source.addToOutgoingEdges(edge);

      Node<T, S> target = nodes.get(edge.getTarget().getId());
      edge.setTarget(target);
      target.addToIncomingEdges(edge);

      edges.put(edge.getId(), edge);
    }

}
