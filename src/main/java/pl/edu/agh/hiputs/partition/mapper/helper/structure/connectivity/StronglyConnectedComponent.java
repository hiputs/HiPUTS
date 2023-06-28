package pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity;

import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class StronglyConnectedComponent implements ConnectedComponent{
  private final Set<String> nodesIds = new HashSet<>();
  @EqualsAndHashCode.Exclude
  private final Set<String> externalEdgesIds = new HashSet<>();

  @Override
  public void addNode(String nodeId) {
    nodesIds.add(nodeId);
  }

  public void addExternalEdge(String externalEdgeId) {
    externalEdgesIds.add(externalEdgeId);
  }
}
