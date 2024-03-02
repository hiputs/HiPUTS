package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

@Service
@RequiredArgsConstructor
public class ReportConsoleAndDelegateDS implements DetectorStrategy {

  private final ReportConsoleDS reportConsoleDS;
  private final DelegateDS delegateDS;

  @Override
  public void execute(DetectorContext detectorContext) {
    reportConsoleDS.execute(detectorContext);
    delegateDS.execute(detectorContext);
  }
}
