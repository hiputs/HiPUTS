package pl.edu.agh.partition.service;

import java.util.Collection;
import java.util.List;
import pl.edu.agh.partition.model.PatchConnectionData;
import pl.edu.agh.partition.model.PatchData;
import pl.edu.agh.partition.model.graph.Graph;

public class SimpleMetisMapFragmentPartitioner implements MapFragmentPartitioner {

  @Override
  public Collection<Graph<PatchData, PatchConnectionData>> partition(Graph<PatchData, PatchConnectionData> graph) {
    return List.of(graph);
  }
}
