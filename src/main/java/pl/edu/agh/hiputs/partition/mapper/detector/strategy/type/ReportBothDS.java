package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

@Service
@RequiredArgsConstructor
public class ReportBothDS implements DetectorStrategy{
  private final ReportConsoleDS reportConsoleDS;
  private final ReportFileDS reportFileDS;

  @Override
  public void execute(DetectorContext detectorContext) {
    reportConsoleDS.execute(detectorContext);
    reportFileDS.execute(detectorContext);
  }
}
