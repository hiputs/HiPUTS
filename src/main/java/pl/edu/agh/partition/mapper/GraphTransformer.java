package pl.edu.agh.partition.mapper;

import pl.edu.agh.partition.model.JunctionData;
import pl.edu.agh.partition.model.WayData;
import pl.edu.agh.partition.model.graph.Graph;

interface GraphTransformer {

    Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph);

}
