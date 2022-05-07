package pl.edu.agh.hiputs.server.partition.model.graph;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Edge<T extends NodeData, S extends EdgeData> {

  private final String id;
  private S data;

  private Node<T, S> source;
  private Node<T, S> target;

}
