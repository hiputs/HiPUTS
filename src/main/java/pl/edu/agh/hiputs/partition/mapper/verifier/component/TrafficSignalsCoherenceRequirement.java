package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicatorEditable;

@Service
@Order(13)
public class TrafficSignalsCoherenceRequirement implements Requirement{

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return areSCCsWithTIsOnIncomingEdges(graph) && areGGsNotEmpty(graph) && areGGsDisjoint(graph) && areGGsComplete(graph);
  }

  @Override
  public String getName() {
    return "13. Traffic signals data completeness and coherence.";
  }

  private boolean areSCCsWithTIsOnIncomingEdges(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream()
        .filter(node -> node.getData().getSignalsControlCenter().isPresent())
        .flatMap(node -> node.getIncomingEdges().stream())
        .allMatch(edge -> edge.getData().getTrafficIndicator().isPresent());
  }

  private boolean areGGsNotEmpty(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream()
        .filter(node -> node.getData().getSignalsControlCenter().isPresent())
        .map(node -> node.getData().getSignalsControlCenter().get())
        .noneMatch(scc -> scc.getGreenColorGroups().isEmpty());
  }

  private boolean areGGsDisjoint(Graph<JunctionData, WayData> graph) {
    Set<TrafficIndicatorEditable> tIs = new HashSet<>();

    return graph.getNodes().values().stream()
        .filter(node -> node.getData().getSignalsControlCenter().isPresent())
        .flatMap(node -> node.getData().getSignalsControlCenter().get().getGreenColorGroups().stream())
        .flatMap(gg -> gg.getTrafficIndicators().stream())
        .allMatch(tIs::add);
  }

  private boolean areGGsComplete(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream()
        .filter(node -> node.getData().getSignalsControlCenter().isPresent())
        .allMatch(node -> node.getData().getSignalsControlCenter().get().getGreenColorGroups().stream()
            .flatMap(gg -> gg.getTrafficIndicators().stream())
            .collect(Collectors.toSet()).equals(node.getIncomingEdges().stream()
                .filter(edge -> edge.getData().getTrafficIndicator().isPresent())
                .map(edge -> edge.getData().getTrafficIndicator().get())
                .collect(Collectors.toSet())));
  }
}
