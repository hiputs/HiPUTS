package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

public interface DetectorStrategy {

  void execute(DetectorContext detectorContext);

}
