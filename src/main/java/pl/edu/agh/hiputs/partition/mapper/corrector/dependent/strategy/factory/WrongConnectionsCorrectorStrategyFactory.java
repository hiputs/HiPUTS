package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.factory;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.WrongConnectionsCorrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.compatibility.TypesFixer;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@RequiredArgsConstructor
public class WrongConnectionsCorrectorStrategyFactory implements CorrectorStrategyFactory<WrongConnectionsCorrector, TypesFixer>{
  private final ModelConfigurationService modelConfigService;
  private final Map<String, TypesFixer> mapOfTypesFixers;

  @Override
  public TypesFixer getFromConfiguration() {
    return mapOfTypesFixers.get(
        modelConfigService.getCorrector2Strategy().get(WrongConnectionsCorrector.class.getSimpleName()));
  }
}
