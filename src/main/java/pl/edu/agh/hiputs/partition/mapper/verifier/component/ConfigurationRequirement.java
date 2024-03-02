package pl.edu.agh.hiputs.partition.mapper.verifier.component;

import java.io.File;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitsRepository;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@Order(15)
@RequiredArgsConstructor
public class ConfigurationRequirement implements Requirement {

  private final ModelConfigurationService modelConfigService;
  private final SpeedLimitsRepository speedLimitsRepository;

  @Override
  public boolean isSatisfying(Graph<JunctionData, WayData> graph) {
    return !modelConfigService.getDetector2Strategy().isEmpty() && !modelConfigService.getCorrector2Strategy().isEmpty()
        && !modelConfigService.getWayTypesPriority().isEmpty()
        && modelConfigService.getModelConfig().getCrossroadMinDistance() > 0
        && modelConfigService.getModelConfig().getDefaultMaxSpeed() > 0
        && modelConfigService.getModelConfig().getWayTypes().length > 0 && new File(
        modelConfigService.getModelConfig().getSpeedLimitsFilePath()).exists()
        && !speedLimitsRepository.getMapOfCountriesWithSpeedLimits().isEmpty();
  }

  @Override
  public String getName() {
    return "15. Model configurable.";
  }
}
