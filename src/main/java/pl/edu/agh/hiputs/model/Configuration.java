package pl.edu.agh.hiputs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

  /**
   * Good knowledge server ip address
   */
  private String serverAddress;

  /**
   * Good knowledge server port
   */
  private int serverPort;

  /**
   * All workers and server exist in the same machine
   */
  private boolean localHostMode;

  /**
   * Local variable not use in JSON file. This flag will by true only when this worker has server task
   */
  private boolean serverOnThisMachine;

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
