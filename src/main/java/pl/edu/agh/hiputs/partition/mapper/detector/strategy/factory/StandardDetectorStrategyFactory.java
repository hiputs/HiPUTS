package pl.edu.agh.hiputs.partition.mapper.detector.strategy.factory;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.Detector;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.type.DetectorStrategy;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@RequiredArgsConstructor
public class StandardDetectorStrategyFactory implements DetectorStrategyFactory{
  private final ModelConfigurationService modelConfigService;
  private final Map<String, DetectorStrategy> mapOfDetectorStrategies;

  @Override
  public DetectorStrategy getFromConfiguration(Class<? extends Detector> determiner) {
    return mapOfDetectorStrategies.get(modelConfigService.getDetector2Strategy().get(determiner.getSimpleName()));
  }
}
