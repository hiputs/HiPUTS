package pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity;

import java.util.Set;

public interface ConnectedComponent {

  Set<String> getNodesIds();

  void addNode(String nodeId);
}
