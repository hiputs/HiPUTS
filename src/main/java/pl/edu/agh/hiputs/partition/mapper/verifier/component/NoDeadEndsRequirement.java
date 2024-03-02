package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(7)
public class NoDeadEndsRequirement implements Requirement {

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream().noneMatch(node -> isDeadIncoming(node) || isDeadOutgoing(node));
  }

  @Override
  public String getName() {
    return "7. No dead end occurs.";
  }

  private boolean isDeadIncoming(Node<JunctionData, WayData> node) {
    return node.getOutgoingEdges().isEmpty() && node.getIncomingEdges().size() == 1;
  }

  private boolean isDeadOutgoing(Node<JunctionData, WayData> node) {
    return node.getIncomingEdges().isEmpty() && node.getOutgoingEdges().size() == 1;
  }
}
