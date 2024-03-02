package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.validator;

import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitRecord;

@Service
@Order(1)
public class DataValidatorCSV implements DataValidator {

  private final static Set<String> formsOfNA = Set.of("NA", "na", "N/A", "n/a", "variable", "");

  @Override
  public void checkAndCorrect(List<SpeedLimitRecord> speedLimits) {
    speedLimits.forEach(record -> {
      // no information about highway speed limit -> look for rural, then urban
      record.setHighway(determineBetterLimit(record.getHighway(), record.getRural(), record.getUrban()));

      // no information about rural speed limit -> look for urban, then highway
      record.setRural(determineBetterLimit(record.getRural(), record.getUrban(), record.getHighway()));

      // no information about urban speed limit -> look for rural, then highway
      record.setUrban(determineBetterLimit(record.getUrban(), record.getRural(), record.getHighway()));
    });

    // null records are useless
    speedLimits.removeIf(this::allContainsNA);
    speedLimits.removeIf(this::countryIdIsEmpty);
  }

  private String determineBetterLimit(String examined, String firstChoice, String secondChoice) {
    if (formsOfNA.contains(examined)) {
      if (!formsOfNA.contains(firstChoice)) {
        return firstChoice;
      }

      return secondChoice;
    }

    return examined;
  }

  private boolean allContainsNA(SpeedLimitRecord speedLimitRecord) {
    return formsOfNA.contains(speedLimitRecord.getHighway()) && formsOfNA.contains(speedLimitRecord.getUrban())
        && formsOfNA.contains(speedLimitRecord.getRural());
  }

  private boolean countryIdIsEmpty(SpeedLimitRecord speedLimitRecord) {
    return StringUtils.isBlank(speedLimitRecord.getCountryId());
  }
}

