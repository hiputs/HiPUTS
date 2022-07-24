package pl.edu.agh.hiputs.partition.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.service.bfs.BFSWithRange;
import pl.edu.agh.hiputs.partition.service.bfs.BFSWithRangeResult;
import pl.edu.agh.hiputs.partition.service.bfs.TimeDistance;
import pl.edu.agh.hiputs.partition.service.util.PatchesGraphExtractor;

@Slf4j
@Service
public class GrowingPatchPartitioner implements PatchPartitioner {

  private final Map<String, Node<PatchData, PatchConnectionData>> patchId2patch = new HashMap<>();

  private final BFSWithRange<JunctionData, WayData> bfsWithRange = new BFSWithRange<>(1000.0, new TimeDistance());

  @Override
  public Graph<PatchData, PatchConnectionData> partition(Graph<JunctionData, WayData> graph) {
    Node<JunctionData, WayData> root = getGraphSource(graph);
    Queue<Node<JunctionData, WayData>> front = new LinkedList<>();

    front.add(root);
    while(!front.isEmpty()) {
      root = front.poll();
      if (root.getOutgoingEdges().stream().allMatch(e -> e.getData().getPatchId() != null)) {
        continue;
      }

      String currentPatchId;

      BFSWithRangeResult<JunctionData, WayData> bfsResult = bfsWithRange.getInRange(graph, root);

      // detakcja kolorów w przeglądanym obszarze (kolory z edgy powinny wystarczyć)
      Set<String> colors = bfsResult.getEdgesInRange()
          .stream()
          .map(e -> e.getData().getPatchId() == null ? "NEW_COLOR" : e.getData().getPatchId())
          .collect(Collectors.toSet());
      colors.remove("NEW_COLOR");
      log.info(String.format("Wykryto %d nowych kolorów", colors.size()));

      // jeśli nie napotkam kolorów to koloruje na nowy
      if(colors.size() == 0) {
        currentPatchId = randomPatchId();
      }
      // jeśli natopotkam w obszarze jeden inny kolor trzeba podjąć decyzje czy kolorować na znaleziony czy możę na nowy. Tymczasowo wybrana opcja druga (loklana optymalizacja)
      else if (colors.size() == 1) {
        currentPatchId = randomPatchId();
      }

      // jeśli napotkam dwa kolory to wybieram "któryś" (tutaj może być bardziej złożony algorytm
      else if (colors.size() == 2) {
        List<String> colorsList = colors.stream().toList();
        currentPatchId = ThreadLocalRandom.current().nextBoolean() ? colorsList.get(0) : colorsList.get(1);
      }
      else {
        throw new IllegalStateException("To many observed colors - patch partitioning failed");
      }


      //pokoloruj wszystkie krawędzie i wirzchołki z oglądanego obszaru + krawędzie wchodzące do kolorowanych wierzchołków
      root.getData().setPatchId(currentPatchId);
      bfsResult.getEdgesInRange().forEach(e -> {
        setPatchIdIfNotSet(e, currentPatchId);
        setPatchIdIfNotSet(e.getTarget(), currentPatchId);
        e.getTarget().getIncomingEdges().forEach(f -> setPatchIdIfNotSet(f, currentPatchId));
      });

      front.addAll(bfsResult.getBorderNodes());
    }

    Graph<PatchData, PatchConnectionData> patchesGraph = new PatchesGraphExtractor().createFrom(graph);
    log.info("Partitioning into patches finished");
    return patchesGraph;
  }

  private Node<JunctionData, WayData> getGraphSource(Graph<JunctionData, WayData> graph) {
    return graph.getNodes()
        .values()
        .stream()
        // .filter(n -> n.getIncomingEdges().size() == 0)
        // .filter(n -> n.getId().equals("1000"))
        .findFirst()
        .get();
  }

  private void setPatchIdIfNotSet(Edge<JunctionData, WayData> edge, String patchId) {
    if(Objects.isNull(edge.getData().getPatchId())) {
      edge.getData().setPatchId(patchId);
    }
  }

  private void setPatchIdIfNotSet(Node<JunctionData, WayData> node, String patchId) {
    if(Objects.isNull(node.getData().getPatchId())) {
      node.getData().setPatchId(patchId);
    }
  }

  private String randomPatchId() {
    return UUID.randomUUID().toString();
  }

}
