package pl.edu.agh.hiputs.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.hiputs.startingUp.StrategyEnum;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class Configuration {

  /**
   * One worker and use test, hardcoded map
   */
  private boolean testMode;

  /**
   * Count of workers
   */
  private int workerCount;

  /**
   * Every worker run visualisation
   */
  private boolean enableGUI;

  /**
   * Server create statistic after simulation
   */
  private boolean statisticModeActive;

  /**
   * How long simulation should work
   */
  private long simulationStep;

  /**
   * Flag decided to read parsed map or parse map from osm file
   */
  private boolean parsedMap;

  /**
   * Path to map only for MultiWorker simulation mode
   */
  private String mapPath;

  public static Configuration getDefault() {
    return Configuration.builder()
        .testMode(true)
        .workerCount(1)
        .enableGUI(true)
        .statisticModeActive(false)
        .simulationStep(Long.MAX_VALUE)
        .build();
  }
}
