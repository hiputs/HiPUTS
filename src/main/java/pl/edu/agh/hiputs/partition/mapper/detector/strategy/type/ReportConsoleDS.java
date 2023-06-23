package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

@Service
public class ReportConsoleDS implements DetectorStrategy{

  @Override
  public void execute(DetectorContext detectorContext) {
    detectorContext.getDetectionReport().ifPresent(System.out::println);
  }
}
