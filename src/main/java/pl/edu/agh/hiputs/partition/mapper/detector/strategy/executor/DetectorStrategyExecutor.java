package pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor;

import pl.edu.agh.hiputs.partition.mapper.detector.Detector;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

public interface DetectorStrategyExecutor {

  void followAppropriateStrategy(Class<? extends Detector> determiner, DetectorContext detectorContext);

}
