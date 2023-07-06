package pl.edu.agh.hiputs.partition.mapper.detector;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.RestrictionsCorrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.RestrictionAware;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.StandardDetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor.DetectorStrategyExecutor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.relation.Restriction;

@Service
@Order(6)
@RequiredArgsConstructor
public class RestrictionsDetector implements Detector{
  private final DetectorStrategyExecutor detectorStrategyExecutor;
  private final RestrictionAware restrictionAware;

  @Override
  public void detect(Graph<JunctionData, WayData> graph) {
    if (detectorStrategyExecutor.isNotExpectedToStart(this.getClass())) {
      return;
    }

    Set<Restriction> foundRestrictions = graph.getNodes().values().stream()
        .flatMap(node -> node.getData().getRestrictions().stream())
        .collect(Collectors.toSet());

    StandardDetectorContext context = new StandardDetectorContext();
    if (!foundRestrictions.isEmpty()) {
      context.setDetectionReport(String.format("%s - found restrictions:\n%s",
          getClass().getSimpleName(), formatReportForRestrictions(foundRestrictions)));

      context.setPreparedCorrector(new RestrictionsCorrector(foundRestrictions, restrictionAware));
    }

    detectorStrategyExecutor.followAppropriateStrategy(this.getClass(), context);
  }

  private String formatReportForRestrictions(Set<Restriction> foundRestrictions) {
    StringBuilder restrictionsStringBuilder = new StringBuilder();
    foundRestrictions.forEach(restriction -> restrictionsStringBuilder.append(
        String.format("\t{fromEdgeId=%s, viaNodeId=%s, toEdgeId=%s, type=%s},\n",
            restriction.getFromEdgeId(),
            restriction.getViaNodeId(),
            restriction.getToEdgeId(),
            restriction.getType()
        )));

    return String.format("[\n%s]\n", restrictionsStringBuilder);
  }
}
