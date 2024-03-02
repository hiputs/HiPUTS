package pl.edu.agh.hiputs.partition.mapper.detector;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.MapConnectivityCorrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.factory.CorrectorStrategyFactory;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.ConnectFixer;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.StandardDetectorContext;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.executor.DetectorStrategyExecutor;
import pl.edu.agh.hiputs.partition.mapper.detector.util.connectivity.CCFinder;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.ConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(2)
@RequiredArgsConstructor
public class MapDisConnectivityDetector implements Detector {

  private final CorrectorStrategyFactory<MapConnectivityCorrector, ConnectFixer> strategyFactory;
  private final DetectorStrategyExecutor detectorStrategyExecutor;
  private final CCFinder<StronglyConnectedComponent> sCCFinder;
  private final CCFinder<WeaklyConnectedComponent> wCCFinder;

  @Override
  public void detect(Graph<JunctionData, WayData> graph) {
    if (detectorStrategyExecutor.isNotExpectedToStart(this.getClass())) {
      return;
    }

    List<StronglyConnectedComponent> sCCsFound = sCCFinder.lookup(graph);
    List<WeaklyConnectedComponent> wCCsFound = wCCFinder.lookup(graph);

    StandardDetectorContext context = new StandardDetectorContext();
    if (sCCsFound.size() > 1 || wCCsFound.size() > 1) {
      context.setDetectionReport(String.format("%s - found connected components:\n%s\n%s", getClass().getSimpleName(),
          formatReportForCCs(sCCsFound, StronglyConnectedComponent.class),
          formatReportForCCs(wCCsFound, WeaklyConnectedComponent.class)));

      context.setPreparedCorrector(
          new MapConnectivityCorrector(sCCsFound, wCCsFound, strategyFactory.getFromConfiguration()));
    }

    detectorStrategyExecutor.followAppropriateStrategy(this.getClass(), context);
  }

  private String formatReportForCCs(List<? extends ConnectedComponent> sCCsFound,
      Class<? extends ConnectedComponent> ccClass) {
    StringBuilder ccsStringBuilder = new StringBuilder();
    sCCsFound.forEach(cc -> {
      ccsStringBuilder.append("\t{");
      cc.getNodesIds().forEach(nodeId -> ccsStringBuilder.append(nodeId).append(", "));
      ccsStringBuilder.append("}, \n");
    });

    return String.format("%ss:\n[\n%s]\n", ccClass.getSimpleName(), ccsStringBuilder);
  }
}