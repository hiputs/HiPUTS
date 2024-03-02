package pl.edu.agh.hiputs.partition.mapper.helper.structure.end;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class DeadEnd {

  private final Node<JunctionData, WayData> nodeStarting;
  private final List<Edge<JunctionData, WayData>> connectingEdges;
}
