package pl.edu.agh.hiputs.partition.model.lights;

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
public class SignalsConfiguration {

  /**
   * Default time for one traffic indicators group to hold green light
   * Used when something goes wrong with assigning time from configuration to real object
   */
  private int defaultTime;

  /**
   * Strategy for switching lights on junctions with traffic signals control center
   */
  private String strategy;

  /**
   * Configuration for each signal control center
   */
  private SignalControlCenterConfiguration[] signalControlCenterConfigurations;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SignalControlCenterConfiguration {

    /**
     * ID of the Node, where signal control center is assigned
     */
    private String id;

    /**
     * Time to hold green light in each traffic indicators group of signal control center assigned to node with above ID
     */
    private int time;
  }
}
