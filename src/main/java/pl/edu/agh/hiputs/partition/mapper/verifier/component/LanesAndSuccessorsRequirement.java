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
@Order(10)
public class LanesAndSuccessorsRequirement implements Requirement {

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return areLanesDefined(graph) && areSuccessorsAssignedOnBends(graph) && areSuccessorsAssignedOnCrossroads(graph)
        && areSuccessorsNotOverlappingWithLanes(graph) && areSuccessorsFromOutgoingEdgesOnly(graph);
  }

  @Override
  public String getName() {
    return "10. Lanes are defined with successors.";
  }

  private boolean areLanesDefined(Graph<JunctionData, WayData> graph) {
    return graph.getEdges().values().stream().noneMatch(edge -> edge.getData().getLanes().isEmpty());
  }

  private boolean areSuccessorsAssignedOnCrossroads(Graph<JunctionData, WayData> graph) {
    return graph.getNodes()
        .values()
        .stream()
        .filter(node -> node.getData().isCrossroad())
        .flatMap(node -> node.getIncomingEdges().stream())
        .flatMap(edge -> edge.getData().getLanes().stream())
        .noneMatch(lane -> lane.getAvailableSuccessors().isEmpty());
  }

  private boolean areSuccessorsAssignedOnBends(Graph<JunctionData, WayData> graph) {
    return graph.getNodes()
        .values()
        .stream()
        .filter(node -> !node.getData().isCrossroad())
        .filter(node -> !node.getOutgoingEdges().isEmpty())
        .flatMap(node -> node.getIncomingEdges().stream())
        .allMatch(
            edge -> edge.getData().getLanes().stream().anyMatch(lane -> !lane.getAvailableSuccessors().isEmpty()));
  }

  private boolean areSuccessorsNotOverlappingWithLanes(Graph<JunctionData, WayData> graph) {
    return graph.getEdges()
        .values()
        .stream()
        .allMatch(edge -> edge.getData()
            .getLanes()
            .stream()
            .flatMap(lane -> lane.getAvailableSuccessors().stream())
            .noneMatch(successor -> edge.getData().getLanes().contains(successor)));
  }

  private boolean areSuccessorsFromOutgoingEdgesOnly(Graph<JunctionData, WayData> graph) {
    return graph.getEdges().values().stream().allMatch(edge -> {
      Set<LaneData> outgoingLanes = edge.getTarget()
          .getOutgoingEdges()
          .stream()
          .flatMap(outgoingEdge -> outgoingEdge.getData().getLanes().stream())
          .collect(Collectors.toSet());

      if (outgoingLanes.isEmpty()) {
        return true;
      }

      return edge.getData()
          .getLanes()
          .stream()
          .flatMap(lane -> lane.getAvailableSuccessors().stream())
          .allMatch(outgoingLanes::contains);
    });
  }
}
