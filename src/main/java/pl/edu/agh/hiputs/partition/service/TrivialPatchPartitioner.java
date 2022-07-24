package pl.edu.agh.hiputs.partition.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.service.util.PatchesGraphExtractor;

@Slf4j
@Service
public class TrivialPatchPartitioner implements PatchPartitioner {

  private final Map<String, Node<PatchData, PatchConnectionData>> patchId2patch = new HashMap<>();

  @Override
  public Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph) {
    log.info("Partitioning into patches started");
    for (Edge<JunctionData, WayData> edge : graph.getEdges().values()) {
      String patchId = randomPatchId();
      if (edge.getTarget().getData().getPatchId() == null) {
        edge.getTarget().getData().setPatchId(patchId);
      }

      if(edge.getSource().getIncomingEdges().isEmpty()) {
        edge.getSource().getData().setPatchId(patchId);
      }
      edge.getData().setPatchId(patchId);
    }

    Graph<PatchData, PatchConnectionData> patchesGraph = new PatchesGraphExtractor().createFrom(graph);
    log.info("Partitioning into patches finished");
    return patchesGraph;
  }

  private String randomPatchId() {
    return UUID.randomUUID().toString();
  }
}
