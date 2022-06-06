package pl.edu.agh.hiputs.service;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.Configuration;

@Slf4j
@Service
public class ConfigurationService {

  private static final String SETTINGS_PATH = "settings.json";
  private static final String OSM_EXTENSION = ".osm";

  @Getter
  private Configuration configuration;

  @PostConstruct
  void init() {
    try {
      File configFile = new File(SETTINGS_PATH);

      if(!configFile.exists()){
        configFile.createNewFile();
        createFileWithDefaultConfiguration(configFile);
        return;
      }

      String json = Files.readString(configFile.toPath())
          .replace(",", ",\n");

      if(Strings.isEmpty(json)){
        createFileWithDefaultConfiguration(configFile);
        return;
      }

      configuration = new Gson().fromJson(json, Configuration.class);
      configuration.setReadFromOsmDirectly(configuration.getMapPath().endsWith(OSM_EXTENSION));
    } catch (IOException e) {
      log.error("Error loading configuration, system use default configuration", e);
      configuration = Configuration.getDefault();
    }
  }

  private void createFileWithDefaultConfiguration(File configFile) {
    configuration = Configuration.getDefault();
    String jsonConfig = new Gson().toJson(configuration)
        .replace(",", ",\n");
    try {
      Files.writeString(configFile.toPath(), jsonConfig);
    } catch (IOException e) {
      log.error("Error creating configuration", e);
    }

  }
}
