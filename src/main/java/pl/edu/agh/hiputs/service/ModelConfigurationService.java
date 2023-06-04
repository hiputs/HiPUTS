package pl.edu.agh.hiputs.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.Getter;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.config.ModelConfiguration;
import pl.edu.agh.hiputs.partition.mapper.config.ModelConfiguration.DataConfiguration;
import pl.edu.agh.hiputs.partition.mapper.config.ModelConfiguration.DataConfiguration.TagEntry;

@Service
public class ModelConfigurationService {
  private final static String SETTINGS_PATH = "model_settings.json";
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ModelConfiguration createDefaultConfig() {
    return ModelConfiguration.builder()
        .wayTypes(new String[]{
            "motorway", "trunk", "motorway_link", "trunk_link", "primary", "secondary", "tertiary",
            "unclassified", "primary_link", "secondary_link", "tertiary_link", "living_street",
            "service", "bus_guideway", "busway", "escape", "raceway", "road", "residential"})
        .wayConditions(DataConfiguration.builder()
            .mandatoryTagEntries(new TagEntry[]{})
            .prohibitedTagEntries(new TagEntry[]{})
            .build())
        .nodeConditions(DataConfiguration.builder()
            .mandatoryTagEntries(new TagEntry[]{})
            .prohibitedTagEntries(new TagEntry[]{})
            .build())
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
