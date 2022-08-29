package pl.edu.agh.hiputs.partition.osm.speed.table.validator;

import java.util.List;
import pl.edu.agh.hiputs.partition.osm.speed.table.repository.SpeedLimitRecord;

public interface DataValidator {

  void checkAndCorrect(List<SpeedLimitRecord> speedLimits);

}
