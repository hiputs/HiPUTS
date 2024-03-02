package pl.edu.agh.hiputs.partition.mapper.transformer;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.helper.service.crossroad.CrossroadDeterminer;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(5)
@RequiredArgsConstructor
public class GraphCrossroadDeterminer implements GraphTransformer {

  private final CrossroadDeterminer crossroadDeterminer;

  @Override
  public Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph) {
    graph.getNodes().values().forEach(node -> node.getData().setCrossroad(crossroadDeterminer.determine(node)));

    return graph;
  }
}
