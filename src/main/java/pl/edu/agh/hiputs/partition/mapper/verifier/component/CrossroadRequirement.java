package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(4)
public class CrossroadRequirement implements Requirement {

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream().allMatch(node -> isCrossroad(node) == node.getData().isCrossroad());
  }

  @Override
  public String getName() {
    return "4. Crossroads with more than two participating ways.";
  }

  private boolean isCrossroad(Node<JunctionData, WayData> node) {
    if (node.getIncomingEdges().size() == 0 || node.getOutgoingEdges().size() == 0) {
      return false;
    }

    Map<Set<Node<JunctionData, WayData>>, Edge<JunctionData, WayData>> nodesParticipating2Edge = new HashMap<>();

    node.getIncomingEdges().forEach(edge -> {
      if (!edge.getSource().equals(edge.getTarget())) {
        nodesParticipating2Edge.put(Set.of(edge.getSource(), edge.getTarget()), edge);
      }
    });
    node.getOutgoingEdges().forEach(edge -> {
      if (!edge.getSource().equals(edge.getTarget())) {
        nodesParticipating2Edge.put(Set.of(edge.getSource(), edge.getTarget()), edge);
      }
    });

    return nodesParticipating2Edge.size() > 2;
  }
}
