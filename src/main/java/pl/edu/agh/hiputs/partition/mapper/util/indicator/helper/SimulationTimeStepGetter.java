package pl.edu.agh.hiputs.partition.mapper.util.indicator.helper;

import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Service
public class SimulationTimeStepGetter {
  public int get() {
    return (int) ConfigurationService.getConfiguration().getSimulationTimeStep();
  }
}
