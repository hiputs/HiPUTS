package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(3)
public class CompletenessRequirement implements Requirement{

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return areSetsFilled(graph) && isTISystemConsistent(graph) &&
        areNodesComplete(graph) && areEdgesComplete(graph) && areLanesConsistent(graph);
  }

  @Override
  public String getName() {
    return "3. Model contains everything needed.";
  }

  private boolean areSetsFilled(Graph<JunctionData, WayData> graph) {
    return !graph.getNodes().isEmpty() && !graph.getEdges().isEmpty() &&
        graph.getEdges().values().stream().anyMatch(edge -> !edge.getData().getLanes().isEmpty());
  }

  private boolean isTISystemConsistent(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream().anyMatch(node -> node.getData().getSignalsControlCenter().isPresent()) ==
        graph.getEdges().values().stream().anyMatch(edge -> edge.getData().getTrafficIndicator().isPresent());
  }

  private boolean areNodesComplete(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream().allMatch(node ->
        node.getIncomingEdges().stream().allMatch(edge -> graph.getEdges().containsKey(edge.getId())) &&
        node.getOutgoingEdges().stream().allMatch(edge -> graph.getEdges().containsKey(edge.getId())));
  }

  private boolean areEdgesComplete(Graph<JunctionData, WayData> graph) {
    return graph.getEdges().values().stream().allMatch(edge ->
        graph.getNodes().containsKey(edge.getSource().getId()) && graph.getNodes().containsKey(edge.getTarget().getId()));
  }

  private boolean areLanesConsistent(Graph<JunctionData, WayData> graph) {
    Set<LaneData> lanes = graph.getEdges().values().stream()
        .flatMap(edge -> edge.getData().getLanes().stream())
        .collect(Collectors.toSet());

    return lanes.stream().allMatch(lane -> lanes.containsAll(lane.getAvailableSuccessors()));
  }
}
