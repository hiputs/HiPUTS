package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end;

import java.util.List;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
public class RemoveDeadEndsFixer implements DeadEndsFixer{

  @Override
  public Graph<JunctionData, WayData> fixFoundDeadEnds(List<DeadEnd> deadEnds, Graph<JunctionData, WayData> graph) {
    deadEnds.forEach(deadEnd ->
        deadEnd.getConnectingEdges().forEach(edge -> removeEdgeFromGraph(edge, graph)));

    return graph;
  }

  private void removeEdgeFromGraph(Edge<JunctionData, WayData> edge, Graph<JunctionData, WayData> graph) {
    if (graph.getEdges().containsKey(edge.getId())) {
      graph.removeEdgeById(edge.getId());
    }

    if (graph.getNodes().containsKey(edge.getSource().getId()) &&
        edge.getSource().getIncomingEdges().isEmpty() &&
        edge.getSource().getOutgoingEdges().isEmpty()) {
      graph.removeNodeById(edge.getSource().getId());
    }

    if (graph.getNodes().containsKey(edge.getTarget().getId()) &&
        edge.getTarget().getIncomingEdges().isEmpty() &&
        edge.getTarget().getOutgoingEdges().isEmpty()) {
      graph.removeNodeById(edge.getTarget().getId());
    }
  }
}
