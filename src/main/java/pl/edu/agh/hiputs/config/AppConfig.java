package pl.edu.agh.hiputs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.edu.agh.hiputs.partition.service.GrowingPatchPartitioner;
import pl.edu.agh.hiputs.partition.service.PatchPartitioner;

@Configuration
public class AppConfig {

  @Bean
  public PatchPartitioner patchPartitioner(){
    return new GrowingPatchPartitioner();
  }

}
