package pl.edu.agh.hiputs.partition.service;

import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface PatchPartitioner {

  Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph);

}
