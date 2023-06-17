package pl.edu.agh.hiputs.partition.mapper.corrector.independent;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group.GreenGroupsAggregator;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator.TrafficIndicatorsCreator;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

@Service
@Order(2)
@RequiredArgsConstructor
public class TrafficSignalsCorrector implements Corrector {
  private final GreenGroupsAggregator aggregator;
  private final TrafficIndicatorsCreator creator;

  @Override
  public Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph) {
    creator.createTIsAndMarkCrossroads(graph);
    aggregator.findAndAggregate(graph);

    return graph;
  }
}
