package pl.edu.agh.hiputs.partition.mapper.detector;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.WrongConnectionsCorrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.factory.CorrectorStrategyFactory;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.compatibility.TypesFixer;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.StandardDetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor.DetectorStrategyExecutor;
import pl.edu.agh.hiputs.partition.mapper.detector.util.compatibility.IncompatibilityFinder;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.compatibility.TypeIncompatibility;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(5)
@RequiredArgsConstructor
public class WrongConnectionsDetector implements Detector{
  private final CorrectorStrategyFactory<WrongConnectionsCorrector, TypesFixer> correctorStrategyFactory;
  private final DetectorStrategyExecutor detectorStrategyExecutor;
  private final List<IncompatibilityFinder> incompatibilityFinders;

  @Override
  public void detect(Graph<JunctionData, WayData> graph) {
    if (detectorStrategyExecutor.isNotExpectedToStart(this.getClass())) {
      return;
    }

    List<TypeIncompatibility> typesIncompatibilities = incompatibilityFinders.stream()
        .flatMap(incompatibilityFinder -> incompatibilityFinder.lookup(graph).stream())
        .toList();

    StandardDetectorContext context = new StandardDetectorContext();
    if (!typesIncompatibilities.isEmpty()) {
      context.setDetectionReport(String.format("%s - found wrong connections:\n%s",
          getClass().getSimpleName(), formatReportForIncompatibilities(typesIncompatibilities)));

      context.setPreparedCorrector(new WrongConnectionsCorrector(
          typesIncompatibilities, correctorStrategyFactory.getFromConfiguration()));
    }

    detectorStrategyExecutor.followAppropriateStrategy(this.getClass(), context);
  }

  private String formatReportForIncompatibilities(List<TypeIncompatibility> incompatibilities) {
    StringBuilder incompatibilitiesStringBuilder = new StringBuilder();
    incompatibilities.forEach(incompatibility -> incompatibilitiesStringBuilder.append(
        String.format("\t{type=%s, edge=%s},\n",
            incompatibility.getRequiredType(),
            incompatibility.getImpactedEdge().getId()
        )));

    return String.format("[\n%s]\n", incompatibilitiesStringBuilder);
  }
}
