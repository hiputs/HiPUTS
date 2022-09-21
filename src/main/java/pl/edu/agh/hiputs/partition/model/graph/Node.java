package pl.edu.agh.hiputs.partition.model.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class Node<T extends NodeData, S extends EdgeData> {

  private final String id;
  private final T data;

  @Setter
  private Double distance = null;
  private Map<String, String> tags;

  private List<Edge<T, S>> incomingEdges = new LinkedList<>();
  private List<Edge<T, S>> outgoingEdges = new LinkedList<>();

  public void merge(Node<T, S> newNode) {
    incomingEdges.addAll(newNode.getIncomingEdges());
    outgoingEdges.addAll(newNode.getOutgoingEdges());
    data.merge(newNode.getData());
  }

  public void mergeDataOnly(Node<T,S> newNode) {
    data.merge(newNode.getData());
  }

  public void addToIncomingEdges(Edge<T, S> edge) {
    if(!incomingEdges.contains(edge)){
      incomingEdges.add(edge);
    }
  }

  public void addToOutgoingEdges(Edge<T, S> edge) {
    if(!outgoingEdges.contains(edge)) {
      outgoingEdges.add(edge);
    }
  }

  public String getTag(String name) {
    return this.tags.get(name);
  }

  public void setTag(String name, String value) {
    this.tags.put(name, value);
  }
}
