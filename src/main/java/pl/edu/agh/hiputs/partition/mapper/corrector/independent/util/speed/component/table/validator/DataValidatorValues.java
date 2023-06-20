package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.validator;

import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitRecord;

@Service
@Order(2)
public class DataValidatorValues implements DataValidator{
  private final static String dashSign = "-";

  @Override
  public void checkAndCorrect(List<SpeedLimitRecord> speedLimits) {
    speedLimits.forEach(record -> {
      // reformatting range of limits to one limit
      record.setHighway(getCorrectSpeedLimit(record.getHighway()));
      record.setUrban(getCorrectSpeedLimit(record.getUrban()));
      record.setRural(getCorrectSpeedLimit(record.getRural()));
    });
  }

  private String getCorrectSpeedLimit(String value) {
    if (value.contains(dashSign)) {
      return value.split(dashSign)[1];
    }

    return value;
  }
}

