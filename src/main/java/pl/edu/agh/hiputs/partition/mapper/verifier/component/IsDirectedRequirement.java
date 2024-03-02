package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(1)
public class IsDirectedRequirement implements Requirement {

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    if (graph.getEdges()
        .values()
        .stream()
        .filter(edge -> !edge.getData().isOneWay())
        .anyMatch(edge -> !graph.getEdges().containsKey(edge.getTarget().getId() + "->" + edge.getSource().getId()))) {
      return false;
    }

    return graph.getEdges()
        .values()
        .stream()
        .filter(edge -> !edge.getData().isOneWay())
        .map(edge -> Pair.of(edge, graph.getEdges().get(edge.getTarget().getId() + "->" + edge.getSource().getId())))
        .allMatch(pair -> areEdgesDifferent(pair.getLeft(), pair.getRight()));
  }

  @Override
  public String getName() {
    return "1. Graph is directed";
  }

  private boolean areEdgesDifferent(Edge<JunctionData, WayData> edge1, Edge<JunctionData, WayData> edge2) {
    return edge1 != edge2 && !edge1.equals(edge2) && edge1.getData().getLanes() != edge2.getData().getLanes() && (
        !edge1.getData().getLanes().equals(edge2.getData().getLanes()) || edge1.getData().getLanes().isEmpty()) && (
        edge1.getData().getTrafficIndicator().isEmpty() || edge2.getData().getTrafficIndicator().isEmpty()
            || !edge1.getData().getTrafficIndicator().get().equals(edge2.getData().getTrafficIndicator().get()));
  }
}
