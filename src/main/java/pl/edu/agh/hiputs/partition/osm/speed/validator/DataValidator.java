package pl.edu.agh.hiputs.partition.osm.speed.validator;

import java.util.List;
import pl.edu.agh.hiputs.partition.osm.speed.repository.SpeedLimitRecord;

public interface DataValidator {

  void checkAndCorrect(List<SpeedLimitRecord> speedLimits);

}
