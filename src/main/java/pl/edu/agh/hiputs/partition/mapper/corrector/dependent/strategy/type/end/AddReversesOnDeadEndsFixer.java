package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.reflector.EdgeReflector;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@RequiredArgsConstructor
public class AddReversesOnDeadEndsFixer implements DeadEndsFixer{
  private final EdgeReflector edgeReflector;

  @Override
  public Graph<JunctionData, WayData> fixFoundDeadEnds(List<DeadEnd> deadEnds, Graph<JunctionData, WayData> graph) {
    deadEnds.forEach(deadEnd ->
        deadEnd.getConnectingEdges().stream()
            .map(edgeReflector::reverseEdge)
            .forEach(edge -> {
              if (checkAdditionPossibility(edge, graph)) {
                graph.addEdge(edge);
              }
            }));

    return graph;
  }

  private boolean checkAdditionPossibility(Edge<JunctionData, WayData> edge, Graph<JunctionData, WayData> graph) {
    return !graph.getEdges().containsKey(edge.getId()) &&
        graph.getNodes().containsKey(edge.getSource().getId()) &&
        graph.getNodes().containsKey(edge.getTarget().getId());
  }
}
