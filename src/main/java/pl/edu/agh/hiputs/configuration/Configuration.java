package pl.edu.agh.hiputs.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.loadbalancer.model.BalancingMode;
import pl.edu.agh.hiputs.model.id.MapFragmentId;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties()
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
   * Count of cores per worker
   * (number of threads used by Scheduler)
   */
  private int coresPerWorkerCount;

  /**
   * Every worker run visualisation
   */
  private boolean enableGUI;

  /**
   * Visualization via Apache Kafka
   */
  private boolean enableVisualization;

  /**
   * Server create statistic after simulation
   */
  private boolean statisticModeActive;

  /**
   * Whether append statistic results at the end of existing files or create new ones
   */
  private boolean appendResults;

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
  private String serverAddress; // todo no one uses it?

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
   * !WORKS ONLY WHEN carGenerator.newGenerator==FALSE!
   * Number of cars to be generated on each lane on simulation start
   */
  private int initialNumberOfCarsPerLane;

  /**
   * !WORKS ONLY WHEN carGenerator.newGenerator==FALSE!
   * Number of cars to be generated on each worker's part of map
   */
  private int numberOfCarsPerWorker;

  /**
   * !WORKS ONLY WHEN carGenerator.newGenerator==FALSE!
   * Number of cars in one of workers - big worker (for LB tests)
   */
  private int numberOfCarsInBigWorker;

  /**
   * Pause between every simulation step in ms. When visualise mode is enabled recommended min 100ms
   */
  private int pauseAfterStep;

  /**
   * View range of car - objects which are further will be not be visible for car
   */
  private int carViewRange;

  /**
   * Default max speed of car in m/s
   */
  private double defaultMaxSpeed;

  /**
   * Maximum acceleration of car in m^2/s
   */
  private double maxAcceleration;

  /**
   * Maximum deceleration of car in m^2/s
   */
  private double maxDeceleration;

  /**
   * Idm distance headway in meters
   */
  private double idmDistanceHeadway;

  /**
   * Idm time headway in seconds
   */
  private double idmTimeHeadway;

  /**
   * Idm delta parameter
   */
  private int idmDelta;

  /**
   * Junction decider time delta factor parameter
   */
  private double junctionSafeTimeDeltaFactor;

  /**
   * Give way threshold in seconds
   */
  private int giveWayThreshold;

  /**
   * Move permanent threshold in seconds set negative value to turn out move permanent when crossroad is locked
   */
  private int movePermanentThreshold;

  /**
   * Time step of simulation in seconds
   */
  private double simulationTimeStep;

  /**
   * Properties related only for patch partitioning process.
   */
  private PatchPartitionerConfiguration patchPartitioner;

  /**
   * Partitioner which divides patches among workers. Possible options `rectangle` and `quadTree`
   */
  private String mapFragmentPartitioner;

  /**
   * Max new cars create after every step
   */
  private int newCars;
  /**
   * Min new cars create after every step
   */
  private int minCars;

  /**
   * Use ticket mechanism to loadbalancing
   */
  private boolean ticketActive;

  /**
   * Path to configuration of time based car generation
   */
  private String timeBasedCarGenerationConfigPath;

  /**
   * Minimum length of generated car
   */
  private double carMinLengthInMeters;

  /**
   * Maximum length of generated car
   */
  private double carMaxLengthInMeters;

  /**
   * Up Boundary of speed that car has when it starts
   */
  private int carUpSpeedBoundaryInMetersPerSecond;

  /**
   * Up Boundary of max speed that car can have
   */
  private int carUpMaxSpeedBoundaryInMetersPerSecond;

  /**
   * Configuration of car generator
   */
  private CarGeneratorConfiguration carGenerator;

  /**
   * Unique worker id
   */
  private MapFragmentId mapFragmentId;

  /**
   * !WORKS ONLY WHEN carGenerator.newGenerator==FALSE!
   * Extend route of each car to maintain all existing vehicles in simulation (when route ends, vehicle disappears)
   */
  private boolean extendCarRouteWhenItEnds;

  /**
   * !!Does not work!! TODO make CarGeneratorServices consistent
   * Create new car in place of a car which ended his journey. Can be used to maintain constant number of cars in
   * simulation.
   */
  private boolean replaceCarWithFinishedRoute;

  public static Configuration getDefault() {
    return Configuration.builder()
        .testMode(false)
        .workerCount(1)
        .coresPerWorkerCount(1)
        .enableGUI(true)
        .enableVisualization(false)
        .statisticModeActive(false)
        .simulationStep(1000)
        .pauseAfterStep(1000)
        .mapPath("")
        .serverAddress("localhost")
        .serverPort(8081)
        .initialNumberOfCarsPerLane(1)
        .carViewRange(300)
        .defaultMaxSpeed(20)
        .maxAcceleration(2.0)
        .maxDeceleration(3.5)
        .idmDistanceHeadway(2)
        .idmTimeHeadway(2)
        .idmDelta(4)
        .junctionSafeTimeDeltaFactor(1.25)
        .giveWayThreshold(10)
        .movePermanentThreshold(50)
        .simulationTimeStep(1.0).balancingMode(BalancingMode.SIMPLY)
        .newCars(0)
        .minCars(0)
        .ticketActive(false)
        .patchPartitioner(PatchPartitionerConfiguration.getDefault())
        .mapFragmentPartitioner("quadTree")
        .timeBasedCarGenerationConfigPath("timeBasedCarGenerationConfig.json")
        .carMinLengthInMeters(3.0)
        .carMaxLengthInMeters(5.0)
        .carUpSpeedBoundaryInMetersPerSecond(28)
        .carUpMaxSpeedBoundaryInMetersPerSecond(38)
        .build();
  }
}
