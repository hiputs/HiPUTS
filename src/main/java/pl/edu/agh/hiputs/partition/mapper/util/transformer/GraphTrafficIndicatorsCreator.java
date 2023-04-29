package pl.edu.agh.hiputs.partition.mapper.util.transformer;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.util.indicator.TIDeterminer;
import pl.edu.agh.hiputs.partition.mapper.util.indicator.TIProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(8)
@RequiredArgsConstructor
public class GraphTrafficIndicatorsCreator implements GraphTransformer{
  private final List<TIProcessor> trafficIndicatorProcessor;
  private final TIDeterminer trafficIndicatorDeterminer;

  // @TODO will be migrated to detector & corrector system in the future

  @Override
  public Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph) {
    trafficIndicatorProcessor.forEach(tiProcessor ->
        graph.getNodes().values().stream()
            .filter(node -> trafficIndicatorDeterminer.checkFromTags(node.getData().getTags()))
            .forEach(tiProcessor::checkAndAllocate));

    return graph;
  }
}
