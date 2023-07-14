package pl.edu.agh.hiputs.partition.mapper.verifier.component;

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
        .noneMatch(edge -> edge.getData().getLength() < modelConfigService.getModelConfig().getCrossroadMinDistance());
  }

  @Override
  public String getName() {
    return String.format("9. No complex crossroad occurs (<%.02fm).",
        modelConfigService.getModelConfig().getCrossroadMinDistance());
  }
}
