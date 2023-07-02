package pl.edu.agh.hiputs.partition.mapper.helper.structure.complex;

import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ComplexCrossroad {
  private final Set<String> nodesIdsIn = new HashSet<>();

  public void addNode(String nodeId) {
    nodesIdsIn.add(nodeId);
  }
}
