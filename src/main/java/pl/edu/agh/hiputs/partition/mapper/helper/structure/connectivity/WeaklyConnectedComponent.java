package pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity;

import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class WeaklyConnectedComponent implements ConnectedComponent {

  private final Set<String> nodesIds = new HashSet<>();

  @Override
  public void addNode(String nodeId) {
    nodesIds.add(nodeId);
  }
}
