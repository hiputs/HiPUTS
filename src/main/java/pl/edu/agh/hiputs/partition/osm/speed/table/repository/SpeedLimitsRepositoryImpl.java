package pl.edu.agh.hiputs.partition.osm.speed.table.repository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.edu.agh.hiputs.partition.osm.speed.table.loader.FileDataLoader;
import pl.edu.agh.hiputs.partition.osm.speed.table.validator.DataValidator;

@Repository
@RequiredArgsConstructor
public class SpeedLimitsRepositoryImpl implements SpeedLimitsRepository{

  private final FileDataLoader fileDataLoader;
  private final List<DataValidator> dataValidators;
  @Getter   // implementing done by getter
  private Map<String, SpeedLimitRecord> mapOfCountriesWithSpeedLimits = Collections.emptyMap();

  @PostConstruct
  private void init() {
    try {
      // 1. Loading stage (from csv file to Java objects)
      List<SpeedLimitRecord> speedLimits = fileDataLoader.readSpeedLimitsFromFile();

      // 2. Validating stage (through all registered validators in specified order)
      dataValidators.forEach(dataValidator -> dataValidator.checkAndCorrect(speedLimits));

      // 3. Reformatting to map format: country -> speed limits
      mapOfCountriesWithSpeedLimits = speedLimits.stream()
          .collect(Collectors.toMap(SpeedLimitRecord::getCountry, Function.identity()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
