package pl.edu.agh.hiputs.partition.osm.speed.validator;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.speed.repository.SpeedLimitRecord;

@Service
@Qualifier("DataValidatorUnits")
public class DataValidatorUnits implements DataValidator{
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
