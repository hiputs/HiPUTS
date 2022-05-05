package pl.edu.agh.hiputs.server.partition.mapper;

import pl.edu.agh.hiputs.server.partition.model.JunctionData;
import pl.edu.agh.hiputs.server.partition.model.WayData;
import pl.edu.agh.hiputs.server.partition.model.graph.Graph;

interface GraphTransformer {

  Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph);

}
