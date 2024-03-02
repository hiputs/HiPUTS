package pl.edu.agh.hiputs.partition.mapper.detector;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.ComplexCrossroadsCorrector;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.StandardDetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor.DetectorStrategyExecutor;
import pl.edu.agh.hiputs.partition.mapper.detector.util.complex.ComplexityFinder;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroadsRepository;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroad;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(4)
@RequiredArgsConstructor
public class ComplexCrossroadsDetector implements Detector {

  private final DetectorStrategyExecutor detectorStrategyExecutor;
  private final List<ComplexityFinder> complexityFinders;
  private final ComplexCrossroadsRepository complexCrossroadsRepository;

  @Override
  public void detect(Graph<JunctionData, WayData> graph) {
    if (detectorStrategyExecutor.isNotExpectedToStart(this.getClass())) {
      return;
    }

    List<ComplexCrossroad> complexCrossroads =
        complexityFinders.stream().flatMap(finder -> finder.lookup(graph).stream()).toList();

    StandardDetectorContext context = new StandardDetectorContext();
    if (!complexCrossroads.isEmpty()) {
      context.setDetectionReport(String.format("%s - found complex crossroads:\n%s", getClass().getSimpleName(),
          formatReportForComplexCrossroads(complexCrossroads)));

      complexCrossroadsRepository.getComplexCrossroads().addAll(complexCrossroads);
      context.setPreparedCorrector(new ComplexCrossroadsCorrector(complexCrossroadsRepository));
    }

    detectorStrategyExecutor.followAppropriateStrategy(this.getClass(), context);
  }

  private String formatReportForComplexCrossroads(List<ComplexCrossroad> complexCrossroads) {
    StringBuilder complexCrossroadsStringBuilder = new StringBuilder();
    complexCrossroads.forEach(complexCrossroad -> complexCrossroadsStringBuilder.append(String.format("\t{%s},\n",
        complexCrossroad.getNodesIdsIn()
            .stream()
            .reduce("", (current, next) -> current.isBlank() ? next : current + String.format(", %s", next)))));

    return String.format("[\n%s]\n", complexCrossroadsStringBuilder);
  }
}