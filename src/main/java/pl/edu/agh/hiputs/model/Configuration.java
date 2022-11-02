package pl.edu.agh.hiputs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.hiputs.loadbalancer.model.BalancingMode;
import pl.edu.agh.hiputs.partition.service.HexagonsPartitioner.BorderEdgesHandlingStrategy;

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
  private int simulationStep;

  /**
   * Flag decided to read parsed map or parse map from osm file
   */
  private transient boolean readFromOsmDirectly;

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
   * Run load balancing strategy   NONE | SIMPLY | PID
   */
  private BalancingMode balancingMode;

  /**
   * Local variable not use in JSON file. This flag will by true only when this worker has server task
   */
  private transient boolean serverOnThisMachine;

  /**
   * Number of cars to be generated on each lane on simulation start
   */
  private int initialNumberOfCarsPerLane;

  /**
   * Pause between every simulation step in ms. When visualise mode is enabled recommended min 100ms
   */
  private int pauseAfterStep;

  /**
   * View range of car - objects which are further will be not be visible for car
   */
  private int carViewRange;

  /**
   * Properties related only for patch partitioning process.
   */
  private PatchPartitionerConfiguration patchPartitionerConfiguration;

  /**
   * Max new cars create after every step in every worker
   */
  private int newCars;

  public static Configuration getDefault() {
    return Configuration.builder()
        .testMode(true)
        .workerCount(1)
        .enableGUI(true)
        .statisticModeActive(false)
        .simulationStep(1000)
        .pauseAfterStep(1000)
        .mapPath("")
        .serverAddress("localhost")
        .serverPort(8081)
        .initialNumberOfCarsPerLane(1)
        .carViewRange(300)
        .balancingMode(BalancingMode.NONE)
        .newCars(15)
        .patchPartitionerConfiguration(PatchPartitionerConfiguration.getDefault())
        .build();
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PatchPartitionerConfiguration {

    /**
     * Patch partitioner name. Supported partitioners are "trivial", "hexagon", "growing"
     */
    private String partitionerType;

    /**
     * Arbitrary maximum distance in meters at which the car should be able to retrieve all necessary information
     * required by decision process
     */
    private double carViewRange;

    /**
     * Property related only for "hexagon" patch partitioner.
     */
    private BorderEdgesHandlingStrategy borderHandlingStrategy;

    public static PatchPartitionerConfiguration getDefault() {
      return PatchPartitionerConfiguration.builder()
          .partitionerType("hexagon")
          .carViewRange(100.0)
          .borderHandlingStrategy(BorderEdgesHandlingStrategy.maxLaneLengthBuffer)
          .build();
    }
  }
}
