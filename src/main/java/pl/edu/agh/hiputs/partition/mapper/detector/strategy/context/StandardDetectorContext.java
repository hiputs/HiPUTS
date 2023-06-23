package pl.edu.agh.hiputs.partition.mapper.detector.strategy.context;

import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;

@Setter
@NoArgsConstructor
public class StandardDetectorContext implements DetectorContext{
  private String detectionReport;
  private Corrector preparedCorrector;

  @Override
  public Optional<String> getDetectionReport() {
    return Optional.ofNullable(detectionReport);
  }

  @Override
  public Optional<Corrector> getPreparedCorrector() {
    return Optional.ofNullable(preparedCorrector);
  }
}
