package pl.edu.agh.hiputs.service.worker.usecase;

import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface MapRepositoryServerHandler {

  void setPatchesGraph(Graph<PatchData, PatchConnectionData> patchesGraph);

}
