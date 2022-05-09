package pl.edu.agh.hiputs.server.partition.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import pl.edu.agh.hiputs.server.partition.model.WayData;
import pl.edu.agh.hiputs.server.partition.model.graph.Edge;
import pl.edu.agh.hiputs.server.partition.model.graph.Graph;
import pl.edu.agh.hiputs.server.partition.model.graph.Node;
import pl.edu.agh.hiputs.server.partition.model.JunctionData;
import pl.edu.agh.hiputs.server.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.server.partition.model.PatchData;

public class TrivialPatchPartitioner implements PatchPartitioner {

  private final Map<String, Node<PatchData, PatchConnectionData>> patchId2patch = new HashMap<>();

  @Override
  public Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph) {
    Graph.GraphBuilder<PatchData, PatchConnectionData> graphBuilder = new Graph.GraphBuilder<>();
    for (Edge<JunctionData, WayData> edge : graph.getEdges().values()) {
      String patchId = randomPatchId();
      edge.getData().setPatchId(patchId);
      edge.getTarget().getData().setPatchId(patchId); //trivial implementation! it is not so obvious

      PatchData patchData = PatchData.builder()
          .graphInsidePatch(
              (new Graph.GraphBuilder<JunctionData, WayData>())
                  .addNode(edge.getTarget())
                  .addEdge(edge)
                  .build())
          .build();
      Node<PatchData, PatchConnectionData> patch = new Node<>(patchId, patchData);

      patchId2patch.put(patchId, patch);
      graphBuilder.addNode(patch);
    }

    //detect edges of patches graph
    patchId2patch.entrySet()
        .stream()
        .map(es -> es.getValue()
            .getData().getGraphInsidePatch()
            .getNodes()
            .values()
            .stream()
            .flatMap(nodes -> nodes.getOutgoingEdges().stream())
            .map(e -> {
              PatchConnectionData patchConnectionData = PatchConnectionData.builder()
                  .throughput((double) e.getData().getMaxSpeed())
                  .build();
              Edge<PatchData, PatchConnectionData> newPatchConnection =
                  new Edge<>(es.getKey() + "->" + e.getData().getPatchId(), patchConnectionData);
              newPatchConnection.setSource(patchId2patch.get(es.getKey()));
              newPatchConnection.setTarget(patchId2patch.get(e.getData().getPatchId()));
              return newPatchConnection;
            })
            .collect(Collectors.toList()))
        .flatMap(Collection::stream)
        .forEach(graphBuilder::addEdge);

    return graphBuilder.build();
  }

  private String randomPatchId() {
    return UUID.randomUUID().toString();
  }
}
