package pl.edu.agh.hiputs.partition.osm.speed.loader;

import com.opencsv.bean.CsvToBeanBuilder;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.osm.speed.repository.SpeedLimitRecord;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Service
@RequiredArgsConstructor
public class FileDataLoaderCSV implements FileDataLoader{
  private static final String CSV_FORMAT = ".csv";

  private final ConfigurationService configurationService;

  @Override
  public List<SpeedLimitRecord> readSpeedLimitsFromFile() throws IOException {
    String fileName = configurationService.getConfiguration().getSpeedLimitsPath();

    if (!fileName.endsWith(CSV_FORMAT)) {
      throw new IOException("Wrong file format.");
    }

    return new CsvToBeanBuilder<SpeedLimitRecord>(new FileReader(fileName))
        .withType(SpeedLimitRecord.class)
        .build()
        .parse();
  }
}
