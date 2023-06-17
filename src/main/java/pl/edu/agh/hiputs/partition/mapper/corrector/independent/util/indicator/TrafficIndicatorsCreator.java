package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.indicator;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface TrafficIndicatorsCreator {

  void createTIsAndMarkCrossroads(Graph<JunctionData, WayData> graph);
}
