package pl.edu.agh.hiputs.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.config.ModelConfiguration;
import pl.edu.agh.hiputs.partition.mapper.config.ModelConfiguration.CorrectorStrategy;
import pl.edu.agh.hiputs.partition.mapper.config.ModelConfiguration.DataConfiguration;
import pl.edu.agh.hiputs.partition.mapper.config.ModelConfiguration.DataConfiguration.TagEntry;
import pl.edu.agh.hiputs.partition.mapper.config.ModelConfiguration.DetectorStrategy;

@Service
public class ModelConfigurationService {

  private final static String SETTINGS_PATH = "model_settings.json";
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  @Getter
  private Map<String, String> detector2Strategy;
  @Getter
  private Map<String, String> corrector2Strategy;
  @Getter
  private Map<String, Integer> wayTypesPriority;
  @Getter
  private ModelConfiguration modelConfig;

  @PostConstruct
  private void load() {
    try {
      File modelConfigFile = new File(SETTINGS_PATH);

      if (modelConfigFile.exists()) {
        modelConfig = gson.fromJson(Files.readString(modelConfigFile.toPath()), ModelConfiguration.class);
      } else {
        modelConfig = createDefaultConfig();
      }

      detector2Strategy = createDetector2StrategyMap();
      corrector2Strategy = createCorrector2StrategyMap();
      wayTypesPriority = createWayTypesPriorityMap();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, String> createDetector2StrategyMap() {
    return Arrays.stream(modelConfig.getDetectorStrategies())
        .collect(Collectors.toMap(detectorStrategy -> StringUtils.capitalize(detectorStrategy.getDetectorName()),
            DetectorStrategy::getStrategyName));
  }

  private Map<String, String> createCorrector2StrategyMap() {
    return Arrays.stream(modelConfig.getCorrectorStrategies())
        .collect(Collectors.toMap(correctorStrategy -> StringUtils.capitalize(correctorStrategy.getCorrectorName()),
            CorrectorStrategy::getStrategyName));
  }

  private Map<String, Integer> createWayTypesPriorityMap() {
    return IntStream.range(0, modelConfig.getWayTypesPriority().length)
        .mapToObj(index -> Pair.of(modelConfig.getWayTypesPriority()[index],
            modelConfig.getWayTypesPriority().length - index))
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  private ModelConfiguration createDefaultConfig() {
    return ModelConfiguration.builder()
        .wayTypes(new String[] {"motorway", "trunk", "motorway_link", "trunk_link", "primary", "secondary", "tertiary",
            "unclassified", "primary_link", "secondary_link", "tertiary_link", "living_street", "service",
            "bus_guideway", "busway", "escape", "raceway", "road", "residential"})
        .wayConditions(DataConfiguration.builder()
            .mandatoryTagEntries(new TagEntry[] {})
            .prohibitedTagEntries(new TagEntry[] {})
            .build())
        .nodeConditions(DataConfiguration.builder()
            .mandatoryTagEntries(new TagEntry[] {})
            .prohibitedTagEntries(new TagEntry[] {})
            .build())
        .defaultMaxSpeed(50)
        .speedLimitsFilePath("data/speedLimits.csv")
        .crossroadMinDistance(10.0)
        .detectorStrategies(
            new DetectorStrategy[] {new DetectorStrategy("tagIncorrectnessDetector", "reportBothAndDelegateDS"),
                new DetectorStrategy("mapDisConnectivityDetector", "reportBothAndDelegateDS"),
                new DetectorStrategy("deadEndsDetector", "reportBothAndDelegateDS"),
                new DetectorStrategy("complexCrossroadsDetector", "reportBothAndDelegateDS"),
                new DetectorStrategy("wrongConnectionsDetector", "reportBothAndDelegateDS"),
                new DetectorStrategy("restrictionsDetector", "reportBothAndDelegateDS")})
        .correctorStrategies(
            new CorrectorStrategy[] {new CorrectorStrategy("mapConnectivityCorrector", "indirectBridgesConnectFixer"),
                new CorrectorStrategy("deadEndsCorrector", "addReversesOnDeadEndsFixer"),
                new CorrectorStrategy("wrongConnectionsCorrector", "changeTypesFixer")})
        .wayTypesPriority(
            new String[] {"motorway", "trunk", "primary", "secondary", "tertiary", "motorway_link", "trunk_link",
                "primary_link", "secondary_link", "tertiary_link"})
        .build();
  }

  @PreDestroy
  private void save() {
    try (FileWriter fileWriter = new FileWriter(SETTINGS_PATH)) {
      gson.toJson(modelConfig, fileWriter);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}