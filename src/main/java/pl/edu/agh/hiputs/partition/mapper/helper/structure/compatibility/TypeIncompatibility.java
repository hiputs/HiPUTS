package pl.edu.agh.hiputs.partition.mapper.helper.structure.compatibility;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class TypeIncompatibility {
  private final String impactedType;
  private final String requiredType;
  private final Edge<JunctionData, WayData> impactedEdge;
}
