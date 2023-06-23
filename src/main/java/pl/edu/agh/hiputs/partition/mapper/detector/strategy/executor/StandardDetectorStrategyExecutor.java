package pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.Detector;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.factory.DetectorStrategyFactory;

@Service
@RequiredArgsConstructor
public class StandardDetectorStrategyExecutor implements DetectorStrategyExecutor {
  private final DetectorStrategyFactory detectorStrategyFactory;

  @Override
  public void followAppropriateStrategy(Class<? extends Detector> determiner, DetectorContext detectorContext) {
    detectorStrategyFactory.getFromConfiguration(determiner).execute(detectorContext);
  }
}
