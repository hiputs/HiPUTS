package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

@Service
@RequiredArgsConstructor
public class ReportBothAndDelegateDS implements DetectorStrategy {

  private final ReportBothDS reportBothDS;
  private final DelegateDS delegateDS;

  @Override
  public void execute(DetectorContext detectorContext) {
    reportBothDS.execute(detectorContext);
    delegateDS.execute(detectorContext);
  }
}
