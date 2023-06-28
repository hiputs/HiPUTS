package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.factory;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.MapConnectivityCorrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.BridgesCreator;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@RequiredArgsConstructor
public class ConnectivityCorrectorStrategyFactory
    implements CorrectorStrategyFactory<MapConnectivityCorrector, BridgesCreator> {
  private final ModelConfigurationService modelConfigService;
  private final Map<String, BridgesCreator> mapOfBridgesCreators;

  @Override
  public BridgesCreator getFromConfiguration() {
    return mapOfBridgesCreators.get(
        modelConfigService.getCorrector2Strategy().get(MapConnectivityCorrector.class.getSimpleName()));
  }
}
