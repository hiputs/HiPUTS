package pl.edu.agh.hiputs.configuration;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import pl.edu.agh.hiputs.partition.service.HexagonsPartitioner;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "patch-partitioner")
@org.springframework.context.annotation.Configuration
public class PatchPartitionerConfiguration {

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
  private HexagonsPartitioner.BorderEdgesHandlingStrategy borderHandlingStrategy;

  public static PatchPartitionerConfiguration getDefault() {
    return PatchPartitionerConfiguration.builder()
        .partitionerType("hexagon")
        .carViewRange(100.0)
        .borderHandlingStrategy(HexagonsPartitioner.BorderEdgesHandlingStrategy.maxLaneLengthBuffer)
        .build();
  }
}

