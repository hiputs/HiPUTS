package pl.edu.agh.partition.service;

import java.util.Collection;
import pl.edu.agh.partition.model.PatchConnectionData;
import pl.edu.agh.partition.model.PatchData;
import pl.edu.agh.partition.model.graph.Graph;

public interface MapFragmentPartitioner {

  Collection<Graph<PatchData, PatchConnectionData>> partition(Graph<PatchData, PatchConnectionData> graph);

}
