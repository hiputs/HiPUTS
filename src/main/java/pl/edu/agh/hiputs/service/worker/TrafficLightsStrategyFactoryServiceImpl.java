package pl.edu.agh.hiputs.service.worker;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.service.SignalsConfigurationService;
import pl.edu.agh.hiputs.service.worker.strategies.TrafficLightsStrategy;
import pl.edu.agh.hiputs.service.worker.usecase.TrafficLightsStrategyFactoryService;

@Service
@RequiredArgsConstructor
public class TrafficLightsStrategyFactoryServiceImpl implements TrafficLightsStrategyFactoryService {

  private final SignalsConfigurationService signalsConfigurationService;
  private final Map<String, TrafficLightsStrategy> strategies;

  @Override
  public TrafficLightsStrategy getByName(String strategyName) {
    return strategies.get(strategyName);
  }

  @Override
  public TrafficLightsStrategy getFromConfiguration() {
    return getByName(signalsConfigurationService.getStrategyName());
  }
}
