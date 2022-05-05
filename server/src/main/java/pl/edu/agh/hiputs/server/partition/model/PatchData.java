package pl.edu.agh.hiputs.server.partition.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.server.partition.model.graph.Edge;
import pl.edu.agh.hiputs.server.partition.model.graph.Node;
import pl.edu.agh.hiputs.server.partition.model.graph.NodeData;

@Getter
@Setter
public class PatchData implements NodeData {

  private Map<String, Node<JunctionData, WayData>> nodes = new HashMap<>();
  private Map<String, Edge<JunctionData, WayData>> edges = new HashMap<>();

  @Override
  public void merge(NodeData other) {

  }

}
