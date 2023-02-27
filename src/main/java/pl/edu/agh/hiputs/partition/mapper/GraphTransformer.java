package pl.edu.agh.hiputs.partition.mapper;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

interface GraphTransformer {

  Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph);

}
