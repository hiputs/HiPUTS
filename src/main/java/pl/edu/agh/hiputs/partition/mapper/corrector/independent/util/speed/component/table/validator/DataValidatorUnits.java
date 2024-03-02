package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.validator;

import java.util.List;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitRecord;

@Service
@Order(3)
public class DataValidatorUnits implements DataValidator {

  private final static Set<String> formsOfMph = Set.of("mph", "MPH");
  private final static double mphToKmhMultiplier = 1.609344;

  @Override
  public void checkAndCorrect(List<SpeedLimitRecord> speedLimits) {
    speedLimits.forEach(record -> {
      if (formsOfMph.contains(record.getSpeedUnit())) {
        record.setSpeedUnit("kph");
        record.setHighway(multiplyMphToKmh(record.getHighway()));
        record.setUrban(multiplyMphToKmh(record.getUrban()));
        record.setRural(multiplyMphToKmh(record.getRural()));
      }
    });
  }

  private String multiplyMphToKmh(String actualValue) {
    return Integer.toString((int) (Integer.parseInt(actualValue) * mphToKmhMultiplier));
  }
}

