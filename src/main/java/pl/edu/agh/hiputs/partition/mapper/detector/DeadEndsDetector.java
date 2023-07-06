package pl.edu.agh.hiputs.partition.mapper.detector;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.DeadEndsCorrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.factory.CorrectorStrategyFactory;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end.DeadEndsFixer;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.StandardDetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor.DetectorStrategyExecutor;
import pl.edu.agh.hiputs.partition.mapper.detector.util.end.DeadEndsFinder;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(3)
@RequiredArgsConstructor
public class DeadEndsDetector implements Detector{
  private final CorrectorStrategyFactory<DeadEndsCorrector, DeadEndsFixer> strategyFactory;
  private final DetectorStrategyExecutor detectorStrategyExecutor;
  private final List<DeadEndsFinder> deadEndsFinders;

  @Override
  public void detect(Graph<JunctionData, WayData> graph) {
    if (detectorStrategyExecutor.isNotExpectedToStart(this.getClass())) {
      return;
    }

    List<DeadEnd> deadEndsFound = deadEndsFinders.stream()
        .flatMap(finder -> finder.lookup(graph).stream())
        .toList();

    StandardDetectorContext context = new StandardDetectorContext();
    if (deadEndsFound.size() > 0) {
      context.setDetectionReport(String.format("%s - found dead ends:\n%s",
          getClass().getSimpleName(), formatReportForDeadEnds(deadEndsFound)));

      context.setPreparedCorrector(new DeadEndsCorrector(deadEndsFound, strategyFactory.getFromConfiguration()));
    }

    detectorStrategyExecutor.followAppropriateStrategy(this.getClass(), context);
  }

  private String formatReportForDeadEnds(List<DeadEnd> deadEnds) {
    StringBuilder deadEndsStringBuilder = new StringBuilder();
    deadEnds.forEach(deadEnd -> deadEndsStringBuilder.append(
        String.format("\t{node=%s, edges=[%s]},\n",
            deadEnd.getNodeStarting().getId(),
            deadEnd.getConnectingEdges().stream()
                .map(Edge::getId)
                .reduce("", (current, next) -> current.isBlank() ?
                    next : current + String.format(", %s", next))
        )));

    return String.format("[\n%s]\n", deadEndsStringBuilder);
  }
}
