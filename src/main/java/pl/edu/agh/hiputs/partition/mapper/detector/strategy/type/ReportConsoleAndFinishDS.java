package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

@Service
@RequiredArgsConstructor
public class ReportConsoleAndFinishDS implements DetectorStrategy{
  private final ReportConsoleDS reportConsoleDS;

  @Override
  public void execute(DetectorContext detectorContext) {
    reportConsoleDS.execute(detectorContext);

    if (detectorContext.getDetectionReport().isPresent()) {
      throw new ModelIssuesException(
          "Detected some issues during tags analysis. Check console output to see more.");
    }
  }
}
