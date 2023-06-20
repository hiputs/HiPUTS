package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.decider;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler.TypeOfRoad;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitRecord;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitsRepository;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@Service
@Order(3)
@RequiredArgsConstructor
public class ResultDecider implements Decider{
  private final SpeedLimitsRepository speedLimitsRepository;
  private final ModelConfigurationService modelConfigService;

  @Override
  public void decideAboutValue(SpeedResultHandler speedDataHandler) {
    SpeedLimitRecord record = speedLimitsRepository.getMapOfCountriesWithSpeedLimits()
        .get(speedDataHandler.getCountry());

    if (speedDataHandler.getTypeOfRoad() == TypeOfRoad.Highway) {
      speedDataHandler.setResultSpeed(Integer.parseInt(record.getHighway()));
    } else if (speedDataHandler.getTypeOfRoad() == TypeOfRoad.Urban) {
      speedDataHandler.setResultSpeed(Integer.parseInt(record.getUrban()));
    } else if (speedDataHandler.getTypeOfRoad() == TypeOfRoad.Rural){
      speedDataHandler.setResultSpeed(Integer.parseInt(record.getRural()));
    } else {
      speedDataHandler.setResultSpeed(modelConfigService.getModelConfig().getDefaultMaxSpeed());
    }
  }
}
