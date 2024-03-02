package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(12)
public class TrafficSignalsExistenceRequirement implements Requirement {

  private final static String highwayKey = "highway";
  private final static String trafficSignalsValue = "traffic_signals";

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return areBendsWithoutSCC(graph) && areCrossroadsWithSCCOnlyWhenNeeded(graph);
  }

  @Override
  public String getName() {
    return "12. Traffic signals only when required.";
  }

  private boolean areBendsWithoutSCC(Graph<JunctionData, WayData> graph) {
    return graph.getNodes()
        .values()
        .stream()
        .filter(node -> !node.getData().isCrossroad())
        .allMatch(node -> node.getData().getSignalsControlCenter().isEmpty());
  }

  private boolean areCrossroadsWithSCCOnlyWhenNeeded(Graph<JunctionData, WayData> graph) {
    return graph.getNodes()
        .values()
        .stream()
        .filter(node -> node.getData().isCrossroad() && node.getData().getSignalsControlCenter().isPresent())
        .allMatch(node -> isCrossroadTaggedAsSCC(node) || isCrossroadDeducedAsSCC(node));
  }

  private boolean isCrossroadTaggedAsSCC(Node<JunctionData, WayData> node) {
    return node.getData().getTags().containsKey(highwayKey) && node.getData()
        .getTags()
        .get(highwayKey)
        .equals(trafficSignalsValue);
  }

  private boolean isCrossroadDeducedAsSCC(Node<JunctionData, WayData> node) {
    return node.getIncomingEdges()
        .stream()
        .flatMap(edge -> getPrecedingBends(edge).stream())
        .distinct()
        .anyMatch(this::isCrossroadTaggedAsSCC);
  }

  private List<Node<JunctionData, WayData>> getPrecedingBends(Edge<JunctionData, WayData> edge) {
    List<Node<JunctionData, WayData>> collectedNodes = new ArrayList<>();
    Optional<Edge<JunctionData, WayData>> workerEdge = Optional.of(edge);

    while (workerEdge.isPresent() && !workerEdge.get().getSource().getData().isCrossroad()) {
      collectedNodes.add(workerEdge.get().getSource());

      Node<JunctionData, WayData> lastNode = workerEdge.get().getTarget();
      workerEdge = workerEdge.get()
          .getSource()
          .getIncomingEdges()
          .stream()
          .filter(candidate -> !candidate.getSource().equals(lastNode))
          .findFirst();
    }

    return collectedNodes;
  }
}
