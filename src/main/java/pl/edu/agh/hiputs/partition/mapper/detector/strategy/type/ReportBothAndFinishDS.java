package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

@Service
@RequiredArgsConstructor
public class ReportBothAndFinishDS implements DetectorStrategy {

  private final ReportBothDS reportBothDS;

  @Override
  public void execute(DetectorContext detectorContext) {
    reportBothDS.execute(detectorContext);

    if (detectorContext.getDetectionReport().isPresent()) {
      throw new ModelIssuesException(
          "Detected some issues during tags analysis. Check console output or find report file to see more.");
    }
  }
}
