package pl.edu.agh.hiputs.partition.osm.speed.loader;

import java.io.IOException;
import java.util.List;
import pl.edu.agh.hiputs.partition.osm.speed.repository.SpeedLimitRecord;

public interface FileDataLoader {

  List<SpeedLimitRecord> readSpeedLimitsFromFile() throws IOException;

}
