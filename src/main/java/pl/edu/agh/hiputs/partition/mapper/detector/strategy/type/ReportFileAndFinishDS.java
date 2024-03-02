package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

@Service
@RequiredArgsConstructor
public class ReportFileAndFinishDS implements DetectorStrategy {

  private final ReportFileDS reportFileDS;

  @Override
  public void execute(DetectorContext detectorContext) {
    reportFileDS.execute(detectorContext);

    if (detectorContext.getDetectionReport().isPresent()) {
      throw new ModelIssuesException("Detected some issues during tags analysis. Find report file to see more.");
    }
  }
}
