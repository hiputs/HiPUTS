package pl.edu.agh.hiputs.partition.mapper;

import java.util.Map;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;

public interface Internal2SimulationModelMapper {

  Map<PatchId, Patch> mapToSimulationModel(Graph<PatchData, PatchConnectionData> graph);
}
