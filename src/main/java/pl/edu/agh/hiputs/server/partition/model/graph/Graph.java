package pl.edu.agh.hiputs.server.partition.model.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

@Getter
public class Graph<T extends NodeData, S extends EdgeData> {

    private final Map<String, Node<T, S>> nodes = new HashMap<>();
    private final Map<String, Edge<T, S>> edges = new HashMap<>();

    public static final class GraphBuilder<T extends NodeData, S extends EdgeData> {

      private final Graph<T, S> graph = new Graph<>();

      public GraphBuilder<T, S> addNode(Node<T, S> newNode) {
        if (!graph.edges.isEmpty()) {
          throw new RuntimeException("Cannot add new node when graph contains edges");
        }

        String newNodeId = newNode.getId();
        if(graph.nodes.containsKey(newNodeId)) {
          graph.nodes.get(newNodeId).merge(newNode);
        } else {
          graph.nodes.put(newNodeId, newNode);
        }
        return this;
      }

      public GraphBuilder<T, S> addEdge(Edge<T, S> edge) {
        Optional.ofNullable(graph.nodes.get(edge.getSource().getId()))
            .ifPresent(source -> {
              edge.setSource(source);
              source.addToOutgoingEdges(edge);
            });

        Optional.ofNullable(graph.nodes.get(edge.getTarget().getId()))
            .ifPresent(target -> {
              edge.setTarget(target);
              target.addToIncomingEdges(edge);
            });

        graph.edges.put(edge.getId(), edge);
        return this;
      }

      public Graph<T, S> build() {
        return graph;
      }
    }

}
