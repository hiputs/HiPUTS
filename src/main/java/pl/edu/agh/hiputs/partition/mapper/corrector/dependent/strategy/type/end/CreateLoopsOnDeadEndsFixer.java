package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.reflector.EdgeReflector;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@RequiredArgsConstructor
public class CreateLoopsOnDeadEndsFixer implements DeadEndsFixer{
  private final EdgeReflector edgeReflector;

  @Override
  public Graph<JunctionData, WayData> fixFoundDeadEnds(List<DeadEnd> deadEnds, Graph<JunctionData, WayData> graph) {
    // adding reverse road to non-reachable start nodes
    deadEnds.stream()
        .map(DeadEnd::getNodeStarting)
        .filter(node -> node.getIncomingEdges().isEmpty() && node.getOutgoingEdges().size() == 1)
        .map(node -> node.getOutgoingEdges().get(0))
        .map(edgeReflector::reverseEdge)
        .filter(reversedEdge -> !graph.getEdges().containsKey(reversedEdge.getId()))
        .forEach(graph::addEdge);

    // adding reverse road to typical dead ends
    deadEnds.stream()
        .map(DeadEnd::getNodeStarting)
        .filter(node -> node.getOutgoingEdges().isEmpty() && node.getIncomingEdges().size() == 1)
        .map(node -> node.getIncomingEdges().get(0))
        .map(edgeReflector::reverseEdge)
        .filter(reversedEdge -> !graph.getEdges().containsKey(reversedEdge.getId()))
        .forEach(graph::addEdge);

    return graph;
  }
}
