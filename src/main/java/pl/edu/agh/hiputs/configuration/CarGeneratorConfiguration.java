package pl.edu.agh.hiputs.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "car-generator")
public class CarGeneratorConfiguration {

  /**
   * Source of car generator. Supported sources are "file" and "live"
   */
  private String generatorSource;

  /**
   * Path to directory where route files are stored.
   */
  private String filesPath;

  /**
   * Flag indicating whether route files should be generated or not.
   */
  private boolean generateRouteFiles;

  /**
   * When `true` CarGeneratorServiceImpl will be used as CarGenerator; when `false` RandomCarGeneratorService will be
   * used.
   */
  private boolean newGenerator;

}
