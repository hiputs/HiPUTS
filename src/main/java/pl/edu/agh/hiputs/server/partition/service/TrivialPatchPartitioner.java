package pl.edu.agh.hiputs.server.partition.service;

import java.util.Collection;
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

  private Map<String, Node<PatchData, PatchConnectionData>> patchId2patch;

  @Override
  public Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph) {
    Graph<PatchData, PatchConnectionData> res = new Graph<>();
    for (Edge<JunctionData, WayData> edge : graph.getEdges().values()) {
      String patchId = randomPatchId();
      edge.getData().setPatchId(patchId);

      Node<PatchData, PatchConnectionData> patch = new Node<>(patchId);
      patch.getData().getEdges().put(edge.getId(), edge);
      patch.getData().getNodes().put(edge.getTarget().getId(), edge.getTarget());
      // patch.getData().getNodes().put(edge.getSource().getId(), edge.getSource());

      patchId2patch.put(patchId, patch);
      res.addNode(patch);
    }

    //detect edges of patches graph
    patchId2patch.entrySet()
        .stream()
        .map(es -> es.getValue()
            .getData()
            .getNodes()
            .values()
            .stream()
            .flatMap(nodes -> nodes.getOutgoingEdges().stream())
            .map(e -> {
              Edge<PatchData, PatchConnectionData> newPatchConnection =
                  new Edge<>(es.getKey() + "->" + e.getData().getPatchId());
              newPatchConnection.setSource(patchId2patch.get(es.getKey()));
              newPatchConnection.setTarget(patchId2patch.get(e.getData().getPatchId()));
              newPatchConnection.getData().setThroughput((double) e.getData().getMaxSpeed());
              return newPatchConnection;
            })
            .collect(Collectors.toList()))
        .flatMap(Collection::stream)
        .forEach(res::addEdge);

    return res;
  }

  private String randomPatchId() {
    return UUID.randomUUID().toString();
  }
}
