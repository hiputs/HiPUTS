package pl.edu.agh.hiputs.service.routegenerator.generator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.service.ConfigurationService;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Optional;

@Component
public class TimeBasedCarGeneratorConfig {

  private List<TimeBasedCarGeneratorConfigEntry> configEntries;

  public Optional<TimeBasedCarGeneratorConfigEntry> getCarsPerLaneAtStep(long step) {
    return configEntries.stream()
      .dropWhile(entry -> step < entry.startStep || entry.endStep < step )
      .findFirst();
  }


  @PostConstruct
  public void init() throws FileNotFoundException {
    var reader = new FileReader(ConfigurationService.getConfiguration().getTimeBasedCarGenerationConfigPath());
    var gson = new Gson();
    var configEntryType = new TypeToken<List<TimeBasedCarGeneratorConfigEntry>>() {}.getType();
    this.configEntries = gson.fromJson(reader, configEntryType);
  }

  @Getter
  @AllArgsConstructor
  final class TimeBasedCarGeneratorConfigEntry {

    private final int carsPerKmLane;
    private final int startStep;
    private final int endStep;

    public int getDuration() {
      return endStep - startStep;
    }

  }
}