package pl.edu.agh.hiputs.service.worker.usecase;

import pl.edu.agh.hiputs.service.worker.strategies.TrafficLightsStrategy;

public interface TrafficLightsStrategyFactoryService {

  TrafficLightsStrategy getByName(String strategyName);

  TrafficLightsStrategy getFromConfiguration();
}
