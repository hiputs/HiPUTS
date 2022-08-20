package pl.edu.agh.hiputs.partition.model.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public class Graph<T extends NodeData, S extends EdgeData> {

    private final Map<String, Node<T, S>> nodes = new HashMap<>();
    private final Map<String, Edge<T, S>> edges = new HashMap<>();

    public void addNode(Node<T,S> newNode) {
      if (nodes.containsKey(newNode.getId())) {
        failNodeOperation(String.format("Node with id %s already exists in graph", newNode.getId()), GraphObjectOperation.ADDING);
      }

      Stream.concat(newNode.getIncomingEdges().stream(), newNode.getOutgoingEdges().stream())
          .forEach(edge -> {
            if (edges.containsKey(edge.getId())) {
              failNodeOperation("Node cannot be added. Reason: Node has connection with edges not existing in graph", GraphObjectOperation.ADDING);
            }
          });

      nodes.put(newNode.getId(), newNode);
    }

    public void addEdge(Edge<T,S> newEdge) {
      if (edges.containsKey(newEdge.getId())) {
        failEdgeOperation(String.format("Edge with id %s already exists in graph", newEdge.getId()), GraphObjectOperation.ADDING);
      }

      Node<T,S> source = nodes.get(newEdge.getSource().getId());
      Node<T,S> target = nodes.get(newEdge.getTarget().getId());

      //todo consider changing equals to ==
      if (source == null || !source.equals(newEdge.getSource())) {
        failEdgeOperation("Edge source not present in graph", GraphObjectOperation.ADDING);
      }

      //todo consider changing equals to ==
      if (target == null || !target.equals(newEdge.getTarget())) {
        failEdgeOperation("Edge target not present in graph", GraphObjectOperation.ADDING);
      }

      source.addToOutgoingEdges(newEdge);
      target.addToIncomingEdges(newEdge);
      this.edges.put(newEdge.getId(), newEdge);
    }

    public Node<T,S> removeNodeById(String nodeId) {
      Node<T,S> nodeToBeRemoved = this.nodes.get(nodeId);
      if(!nodeToBeRemoved.getIncomingEdges().isEmpty() || !nodeToBeRemoved.getOutgoingEdges().isEmpty()) {
        failNodeOperation("Node has connection with edges", GraphObjectOperation.REMOVING);
      }
      nodes.remove(nodeId);
      return nodeToBeRemoved;
    }

    public Edge<T,S> removeEdgeById(String edgeId) {
      Edge<T, S> edgeToBeRemoved = this.edges.get(edgeId);
      //todo checks for safety of this operation
      edgeToBeRemoved.getSource().getOutgoingEdges().remove(edgeToBeRemoved);
      edgeToBeRemoved.getTarget().getIncomingEdges().remove(edgeToBeRemoved);
      edges.remove(edgeId);
      return edgeToBeRemoved;
    }

    private void failNodeOperation(String reason, GraphObjectOperation op) {
      fail(reason, "Node", op);
    }

    private void failEdgeOperation(String reason, GraphObjectOperation op) {
      fail(reason, "Edge", op);
    }

    private void fail(String reason, String addedObjectType, GraphObjectOperation op) {
      throw new IllegalArgumentException(addedObjectType + " cannot " + op.passiveForm() + ". Reason: " + reason);
    }

    private enum GraphObjectOperation {
      ADDING {
        @Override
        public String passiveForm() {
          return "be added";
        }
      }, REMOVING {
        @Override
        public String passiveForm() {
          return "be removed";
        }
      };

      public abstract String passiveForm();
    }

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
        //todo remove that check - multiple roads connecting same nodes should be handled in simulation
        if (graph.getEdges().containsKey(edge.getId())) {
          return this;
        }

        Optional.ofNullable(graph.nodes.get(edge.getSource().getId()))
            .ifPresent(source -> {
              source.mergeDataOnly(edge.getSource());
              edge.setSource(source);
              source.addToOutgoingEdges(edge);
            });

        Optional.ofNullable(graph.nodes.get(edge.getTarget().getId()))
            .ifPresent(target -> {
              target.mergeDataOnly(edge.getTarget());
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
