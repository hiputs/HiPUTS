package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@Order(2)
@RequiredArgsConstructor
public class OnlyNeededRequirement implements Requirement{
  private final static String highwayKey = "highway";
  private final ModelConfigurationService modelConfigService;

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return graph.getNodes().values().stream().allMatch(this::isNodeComplete) &&
        graph.getEdges().values().stream().allMatch(this::isEdgeComplete);
  }

  @Override
  public String getName() {
    return "2. Only important ways and nodes in graph.";
  }

  private boolean isNodeComplete(Node<JunctionData, WayData> node) {
    return !node.getIncomingEdges().isEmpty() || !node.getOutgoingEdges().isEmpty();
  }

  private boolean isEdgeComplete(Edge<JunctionData, WayData> edge) {
    return Objects.nonNull(edge.getSource()) && Objects.nonNull(edge.getTarget()) &&
        edge.getData().getTags().containsKey(highwayKey) &&
        Arrays.stream(modelConfigService.getModelConfig().getWayTypes()).anyMatch(wayType ->
            edge.getData().getTags().get(highwayKey).equals(wayType));
  }
}
