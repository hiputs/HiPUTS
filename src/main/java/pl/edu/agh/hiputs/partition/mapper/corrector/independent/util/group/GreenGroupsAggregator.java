package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.group;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface GreenGroupsAggregator {

  void findAndAggregate(Graph<JunctionData, WayData> graph);
}
