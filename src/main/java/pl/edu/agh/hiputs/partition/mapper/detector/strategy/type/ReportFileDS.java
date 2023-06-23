package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;

@Service
public class ReportFileDS implements DetectorStrategy{
  private final static String reportFilePath = "model_report.txt";
  private final List<String> collectedReports = new ArrayList<>();

  @Override
  public void execute(DetectorContext detectorContext) {
    detectorContext.getDetectionReport().ifPresent(collectedReports::add);
  }

  @PreDestroy
  private void savingCollectedData() {
    if (!collectedReports.isEmpty()) {
      try (PrintWriter writer = new PrintWriter(new FileWriter(reportFilePath, false))) {
        collectedReports.forEach(writer::println);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
