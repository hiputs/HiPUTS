package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

@Service
@RequiredArgsConstructor
public class ReportFileAndDelegateDS implements DetectorStrategy {

  private final ReportFileDS reportFileDS;
  private final DelegateDS delegateDS;

  @Override
  public void execute(DetectorContext detectorContext) {
    reportFileDS.execute(detectorContext);
    delegateDS.execute(detectorContext);
  }
}
