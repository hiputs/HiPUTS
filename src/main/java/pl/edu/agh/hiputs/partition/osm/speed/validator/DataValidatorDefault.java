package pl.edu.agh.hiputs.partition.osm.speed.validator;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.speed.repository.SpeedLimitRecord;

@Service
@Qualifier("DataValidatorDefault")
@Order(4)
public class DataValidatorDefault implements DataValidator{

  @Override
  public void checkAndCorrect(List<SpeedLimitRecord> speedLimits) {
    SpeedLimitRecord defaultOne = new SpeedLimitRecord();
    defaultOne.setCountry("DEFAULT");
    defaultOne.setHighway(getMostUsedLimit(speedLimits, SpeedLimitRecord::getHighway));
    defaultOne.setRural(getMostUsedLimit(speedLimits, SpeedLimitRecord::getRural));
    defaultOne.setUrban(getMostUsedLimit(speedLimits, SpeedLimitRecord::getUrban));
    defaultOne.setSpeedUnit("kph");
    speedLimits.add(defaultOne);
  }

  private String getMostUsedLimit(List<SpeedLimitRecord> speedLimits, Function<SpeedLimitRecord, String> attribute) {
    return speedLimits.stream()
        .map(attribute)
        .filter(NumberUtils::isCreatable)
        .collect(Collectors.groupingBy(speed -> speed, Collectors.counting()))
        .entrySet().stream()
        .max(Entry.comparingByValue())
        .orElseThrow().getKey();
  }
}
