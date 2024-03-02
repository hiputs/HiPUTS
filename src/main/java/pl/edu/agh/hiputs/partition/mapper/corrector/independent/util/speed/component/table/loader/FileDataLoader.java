package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.loader;

import java.io.IOException;
import java.util.List;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitRecord;

public interface FileDataLoader {

  List<SpeedLimitRecord> readSpeedLimitsFromFile() throws IOException;

}


