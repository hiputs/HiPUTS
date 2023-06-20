package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.validator;

import java.util.List;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitRecord;

public interface DataValidator {

  void checkAndCorrect(List<SpeedLimitRecord> speedLimits);

}

