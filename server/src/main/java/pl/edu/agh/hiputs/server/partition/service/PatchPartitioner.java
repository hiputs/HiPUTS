package pl.edu.agh.hiputs.server.partition.service;

import pl.edu.agh.hiputs.server.partition.model.JunctionData;
import pl.edu.agh.hiputs.server.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.server.partition.model.PatchData;
import pl.edu.agh.hiputs.server.partition.model.WayData;
import pl.edu.agh.hiputs.server.partition.model.graph.Graph;

public interface PatchPartitioner {

  Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph);

}
