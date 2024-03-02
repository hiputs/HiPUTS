package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component.TIDeterminer;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.component.TIProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@RequiredArgsConstructor
public class StandardTrafficIndicatorsCreator implements TrafficIndicatorsCreator {
  private final List<TIProcessor> trafficIndicatorProcessor;
  private final TIDeterminer trafficIndicatorDeterminer;

  @Override
  public void createTIsAndMarkCrossroads(Graph<JunctionData, WayData> graph) {
    trafficIndicatorProcessor.forEach(tiProcessor ->
        graph.getNodes().values().stream()
            .filter(node -> trafficIndicatorDeterminer.checkFromTags(node.getData().getTags()))
            .forEach(tiProcessor::checkAndAllocate));
  }
}
