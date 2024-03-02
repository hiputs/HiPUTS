package pl.edu.agh.hiputs.partition.mapper.detector.strategy.context;

import java.util.Optional;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;

public interface DetectorContext {

  Optional<String> getDetectionReport();

  Optional<Corrector> getPreparedCorrector();
}
