package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority.component;

import java.util.List;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
@Order(2)
public class LanesPriorityProcessor extends AbstractPriorityProcessor {

  @Override
  public Optional<Edge<JunctionData, WayData>> compareRoads(List<Edge<JunctionData, WayData>> edges) {
    return compareRoadsByValue(edges, edge -> edge.getData().getLanes().size());
  }
}
