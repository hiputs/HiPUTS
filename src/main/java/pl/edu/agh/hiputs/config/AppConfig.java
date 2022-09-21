package pl.edu.agh.hiputs.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.edu.agh.hiputs.model.Configuration.PatchPartitionerConfiguration;
import pl.edu.agh.hiputs.partition.service.GrowingPatchPartitioner;
import pl.edu.agh.hiputs.partition.service.HexagonsPartitioner;
import pl.edu.agh.hiputs.partition.service.PatchPartitioner;
import pl.edu.agh.hiputs.partition.service.TrivialPatchPartitioner;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

  private final ConfigurationService configurationService;

  @Bean
  public PatchPartitioner patchPartitioner(){
    PatchPartitionerConfiguration partitionerConf = configurationService.getConfiguration().getPatchPartitionerConfiguration();
    if (partitionerConf.getPartitionerType().equals("hexagon")) {
      return new HexagonsPartitioner(partitionerConf.getBorderHandlingStrategy(), partitionerConf.getCarViewRange());
    }
    if (partitionerConf.getPartitionerType().equals("growing")) {
      return new GrowingPatchPartitioner();
    }
    if (partitionerConf.getPartitionerType().equals("trivial")) {
      return new TrivialPatchPartitioner();
    }
    return null;
  }

}
