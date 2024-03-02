package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.factory;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.DeadEndsCorrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end.DeadEndsFixer;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@RequiredArgsConstructor
public class DeadEndsCorrectorStrategyFactory implements CorrectorStrategyFactory<DeadEndsCorrector, DeadEndsFixer> {

  private final ModelConfigurationService modelConfigService;
  private final Map<String, DeadEndsFixer> mapOfDeadEndsFixers;

  @Override
  public DeadEndsFixer getFromConfiguration() {
    return mapOfDeadEndsFixers.get(
        modelConfigService.getCorrector2Strategy().get(DeadEndsCorrector.class.getSimpleName()));
  }
}
