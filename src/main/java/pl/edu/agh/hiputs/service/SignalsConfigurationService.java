package pl.edu.agh.hiputs.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.lights.SignalsConfiguration;
import pl.edu.agh.hiputs.partition.model.lights.SignalsConfiguration.SignalControlCenterConfiguration;

@Service
public class SignalsConfigurationService {
  private final static Integer minRandTime = 10;
  private final static Integer maxRandTime = 40;
  private final String SETTINGS_PATH = "signals_settings.json";

  private final Random randomGenerator = new Random();
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private Map<String, Integer> nodeId2TimeMap;
  private SignalsConfiguration signalsConfig;

  @PostConstruct
  private void load() {
    try {
      File signalsConfigFile = new File(SETTINGS_PATH);

      if (signalsConfigFile.exists()) {
        signalsConfig = new Gson().fromJson(Files.readString(signalsConfigFile.toPath()), SignalsConfiguration.class);

        nodeId2TimeMap = Arrays.stream(signalsConfig.getSignalControlCenterConfigurations())
            .collect(Collectors.toMap(
                SignalControlCenterConfiguration::getId,
                SignalControlCenterConfiguration::getTime
            ));
      } else {
        signalsConfig = new SignalsConfiguration();
        signalsConfig.setDefaultTime(randIntegerFromInterval());
        signalsConfig.setStrategy("redGreenOnlyTrafficLightsStrategy");

        nodeId2TimeMap = new HashMap<>();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public int getTimeForSpecificNode(String nodeId) {
    if (!nodeId2TimeMap.containsKey(nodeId)) {
      nodeId2TimeMap.put(nodeId, randIntegerFromInterval());
    }

    return nodeId2TimeMap.getOrDefault(nodeId, signalsConfig.getDefaultTime());
  }

  public String getStrategyName() {
    return signalsConfig.getStrategy();
  }

  private int randIntegerFromInterval() {
    return randomGenerator.nextInt(maxRandTime - minRandTime) + minRandTime;
  }

  @PreDestroy
  private void save() {
    try (FileWriter fileWriter = new FileWriter(SETTINGS_PATH)) {
      signalsConfig.setSignalControlCenterConfigurations(nodeId2TimeMap.entrySet().stream()
          .map(entry -> new SignalControlCenterConfiguration(entry.getKey(), entry.getValue()))
          .toArray(SignalControlCenterConfiguration[]::new));

      gson.toJson(signalsConfig, fileWriter);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
