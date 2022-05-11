package pl.edu.agh.hiputs.partition.service;

import java.util.Collection;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface MapFragmentPartitioner {

  Collection<Graph<PatchData, PatchConnectionData>> partition(Graph<PatchData, PatchConnectionData> graph);

}
