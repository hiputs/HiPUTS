package pl.edu.agh.hiputs.partition.mapper.detector.strategy.factory;

import pl.edu.agh.hiputs.partition.mapper.detector.Detector;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.type.DetectorStrategy;

public interface DetectorStrategyFactory {

  DetectorStrategy getFromConfiguration(Class<? extends Detector> determiner);

}
