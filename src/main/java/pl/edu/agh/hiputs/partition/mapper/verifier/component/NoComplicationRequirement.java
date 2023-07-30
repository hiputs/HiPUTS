package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@Order(9)
@RequiredArgsConstructor
public class NoComplicationRequirement implements Requirement{
  private final ModelConfigurationService modelConfigService;

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return graph.getEdges().values().stream()
        .filter(edge -> edge.getSource().getData().isCrossroad() && edge.getTarget().getData().isCrossroad())
        .noneMatch(edge -> {
          try {
            // measurement between nodes created during crossroad simplifying should be disabled (they have UUID)
            UUID sourceId = UUID.fromString(edge.getSource().getId());
            return false;
          } catch (IllegalArgumentException ignored) {}

          try {
            // measurement between nodes created during crossroad simplifying should be disabled (they have UUID)
            UUID targetId = UUID.fromString(edge.getTarget().getId());
            return false;
          } catch (IllegalArgumentException ignored) {}

          return edge.getData().getLength() < modelConfigService.getModelConfig().getCrossroadMinDistance();
        });
  }

  @Override
  public String getName() {
    return String.format("9. No complex crossroad occurs (<%.02fm).",
        modelConfigService.getModelConfig().getCrossroadMinDistance());
  }
}
