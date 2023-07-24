package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(8)
public class RelationsRequirement implements Requirement{

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream()
        .filter(node -> node.getData().isCrossroad())
        .filter(node -> !node.getData().getRestrictions().isEmpty())
        .allMatch(this::areRestrictionsConsidered);
  }

  @Override
  public String getName() {
    return "8. Relations are taken into consideration.";
  }

  private boolean areRestrictionsConsidered(Node<JunctionData, WayData> node) {
    Map<String, Edge<JunctionData, WayData>> edges2ConnectingNode = Stream.concat(
        node.getIncomingEdges().stream(), node.getOutgoingEdges().stream())
        .collect(Collectors.toMap(Edge::getId, Function.identity()));

    return node.getData().getRestrictions().stream()
        .allMatch(restriction -> {
          Edge<JunctionData, WayData> fromEdge = edges2ConnectingNode.getOrDefault(restriction.getFromEdgeId(), null);
          Edge<JunctionData, WayData> toEdge = edges2ConnectingNode.getOrDefault(restriction.getToEdgeId(), null);

          if (Objects.isNull(fromEdge) || Objects.isNull(toEdge)) {
            // they could exist earlier but be simplified
            return true;
          }

          if (restriction.getType().toString().startsWith("ONLY_")) {
            return new HashSet<>(toEdge.getData().getLanes()).containsAll(fromEdge.getData().getLanes().stream()
                .flatMap(lane -> lane.getAvailableSuccessors().stream())
                .collect(Collectors.toSet()));
          } else if (restriction.getType().toString().startsWith("NO_")) {
            return !new HashSet<>(toEdge.getData().getLanes()).containsAll(fromEdge.getData().getLanes().stream()
                .flatMap(lane -> lane.getAvailableSuccessors().stream())
                .collect(Collectors.toSet()));
          } else {
            // unexpected restriction type should not be considered and it is desirable situation
            return true;
          }
        });
  }
}
