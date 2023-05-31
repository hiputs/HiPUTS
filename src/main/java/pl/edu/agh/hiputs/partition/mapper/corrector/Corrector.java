package pl.edu.agh.hiputs.partition.mapper.corrector;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface Corrector {

  Graph<JunctionData, WayData> correct(Graph<JunctionData, WayData> graph);
}
