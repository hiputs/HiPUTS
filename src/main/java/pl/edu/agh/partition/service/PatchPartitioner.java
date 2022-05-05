package pl.edu.agh.partition.service;

import pl.edu.agh.partition.model.PatchConnectionData;
import pl.edu.agh.partition.model.PatchData;
import pl.edu.agh.partition.model.graph.Graph;
import pl.edu.agh.partition.model.JunctionData;
import pl.edu.agh.partition.model.WayData;

public interface PatchPartitioner {

    Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph);

}
