package pl.edu.agh.hiputs.partition.mapper.corrector.independent;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority.PriorityProcessorChain;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(5)
@RequiredArgsConstructor
public class RoadsPriorityCorrector implements Corrector {
  private final PriorityProcessorChain priorityChain;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    graph.getNodes().values().stream()
        .filter(node -> node.getData().isCrossroad())
        .map(Node::getIncomingEdges)
        .map(priorityChain::getTopPriorityRoad)
        .filter(Objects::nonNull)
        .forEach(edge -> edge.getData().setPriorityRoad(true));

    return graph;
  }
}
