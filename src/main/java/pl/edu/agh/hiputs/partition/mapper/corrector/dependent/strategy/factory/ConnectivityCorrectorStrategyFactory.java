package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.factory;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.MapConnectivityCorrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.ConnectFixer;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@RequiredArgsConstructor
public class ConnectivityCorrectorStrategyFactory
    implements CorrectorStrategyFactory<MapConnectivityCorrector, ConnectFixer> {

  private final ModelConfigurationService modelConfigService;
  private final Map<String, ConnectFixer> mapOfBridgesCreators;

  @Override
  public ConnectFixer getFromConfiguration() {
    return mapOfBridgesCreators.get(
        modelConfigService.getCorrector2Strategy().get(MapConnectivityCorrector.class.getSimpleName()));
  }
}
