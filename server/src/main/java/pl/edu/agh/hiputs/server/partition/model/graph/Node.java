package pl.edu.agh.hiputs.server.partition.model.graph;

import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Node<T extends NodeData, S extends EdgeData> {

  private final String id;
  private T data;

  @Setter(AccessLevel.NONE)
  private List<Edge<T, S>> incomingEdges = new LinkedList<>();
  @Setter(AccessLevel.NONE)
  private List<Edge<T, S>> outgoingEdges = new LinkedList<>();


  public void merge(Node<T, S> newNode) {
    incomingEdges.addAll(newNode.getIncomingEdges());
    outgoingEdges.addAll(newNode.getOutgoingEdges());
    data.merge(newNode.getData());
  }

  public void addToIncomingEdges(Edge<T, S> edge) {
    incomingEdges.add(edge);
  }

  public void addToOutgoingEdges(Edge<T, S> edge) {
    outgoingEdges.add(edge);
  }


}
