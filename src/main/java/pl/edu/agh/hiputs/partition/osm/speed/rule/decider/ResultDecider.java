package pl.edu.agh.hiputs.partition.osm.speed.rule.decider;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.TypeOfRoad;
import pl.edu.agh.hiputs.partition.osm.speed.table.repository.SpeedLimitRecord;
import pl.edu.agh.hiputs.partition.osm.speed.table.repository.SpeedLimitsRepository;

@Service
@Order(3)
@RequiredArgsConstructor
public class ResultDecider implements Decider{
  private final SpeedLimitsRepository speedLimitsRepository;

  @Override
  public void decideAboutValue(SpeedResultHandler speedDataHandler) {
    SpeedLimitRecord record = speedLimitsRepository.getMapOfCountriesWithSpeedLimits()
        .get(speedDataHandler.getCountry());

    if (speedDataHandler.getTypeOfRoad() == TypeOfRoad.Highway) {
      speedDataHandler.setResultSpeed(record.getHighway());
    } else if (speedDataHandler.getTypeOfRoad() == TypeOfRoad.Urban) {
      speedDataHandler.setResultSpeed(record.getUrban());
    } else {
      speedDataHandler.setResultSpeed(record.getRural());
    }
  }
}
