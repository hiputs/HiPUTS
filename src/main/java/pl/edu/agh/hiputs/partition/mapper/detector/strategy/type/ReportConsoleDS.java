package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

@Service
@Slf4j(topic = "IssuesConsolePrinter")
public class ReportConsoleDS implements DetectorStrategy{

  @Override
  public void execute(DetectorContext detectorContext) {
    detectorContext.getDetectionReport().ifPresent(log::warn);
  }
}
