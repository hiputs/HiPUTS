package pl.edu.agh.hiputs.partition.mapper.config;

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
public class ModelConfiguration {

  /**
   * All allowed types of ways to import from OSM
   */
  private String[] wayTypes;

  /**
   * Conditions which have to be met for all the ways during import
   */
  private DataConfiguration wayConditions;

  /**
   * Conditions which have to be met for all the nodes during import
   */
  private DataConfiguration nodeConditions;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DataConfiguration {

    /**
     * Mappings key->value, which should be present in tags map
     */
    private TagEntry[] mandatoryTagEntries;

    /**
     * Mappings key->value, which should not be present in tags map
     */
    private TagEntry[] prohibitedTagEntries;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagEntry {

      /**
       * Tag key
       */
      private String key;

      /**
       * Tag value
       */
      private String value;
    }
  }

  /**
   * Default speed limit for all roads where no implemented conditions were met
   */
  private Integer defaultMaxSpeed;

  /**
   * A path to file containing speed limits for all types of road and countries
   */
  private String speedLimitsFilePath;

  /**
   * Default speed limit for all roads where no implemented conditions were met
   */
  private Double crossroadMinDistance;

  /**
   * Names mappings of detector -> strategy
   */
  private DetectorStrategy[] detectorStrategies;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DetectorStrategy {

    /**
     * Name of detector to which we assign strategy
     */
    private String detectorName;

    /**
     * Name of strategy used for detector with provided name
     */
    private String strategyName;
  }

  /**
   * Names mappings of corrector -> strategy
   */
  private CorrectorStrategy[] correctorStrategies;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CorrectorStrategy {

    /**
     * Name of corrector to which we assign strategy
     */
    private String correctorName;

    /**
     * Name of strategy used for corrector with provided name
     */
    private String strategyName;
  }

  /**
   * Priority order for types of ways
   */
  private String[] wayTypesPriority;
}
