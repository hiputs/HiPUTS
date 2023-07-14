package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(14)
public class RoadsPriorityRequirement implements Requirement{

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream()
        .filter(node -> node.getData().isCrossroad())
        .allMatch(node -> node.getIncomingEdges().stream()
            .anyMatch(edge -> edge.getData().isPriorityRoad()));
  }

  @Override
  public String getName() {
    return "14. Roads priority on crossroads.";
  }
}
