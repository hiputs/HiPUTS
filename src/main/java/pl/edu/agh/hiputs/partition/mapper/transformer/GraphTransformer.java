package pl.edu.agh.hiputs.partition.mapper.transformer;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface GraphTransformer {

  Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph);

}
