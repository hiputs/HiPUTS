package pl.edu.agh.hiputs.partition.service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
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


@Slf4j
public class PatchCorrectnessChecker {

  private final BFSWithRange<JunctionData, WayData> bfsWithRange = new BFSWithRange<>(1050.0, new TimeDistance());

  public boolean testAllPatches(
      Graph<PatchData, PatchConnectionData> patchesGraph,
      Graph<JunctionData, WayData> mapGraph) {
    int incorrectByVisibilityCheckCount = 0;
    int incorrectByInsideCheck = 0;
    for(String patchId : patchesGraph.getNodes().keySet()) {
      if (testVisibilityCorrectness(patchesGraph, mapGraph, patchId)) {
        log.info(String.format("Patch with id=%s is correct by visibilityCheck", patchId));
      } else {
        log.warn(String.format("Patch with id=%s is incorrect by visibilityCheck", patchId));
        incorrectByVisibilityCheckCount++;
      }

      if (testInsideCorrectness(patchesGraph, mapGraph, patchId)) {
        log.info(String.format("Patch with id=%s is correct by insideCheck", patchId));
      } else {
        log.warn(String.format("Patch with id=%s is incorrect by insideCheck", patchId));
        incorrectByInsideCheck++;
      }
    }
    log.info(String.format("Patch partitioning result by visibility check:  %d/%d (incorrect/all)", incorrectByVisibilityCheckCount, patchesGraph.getNodes().values().size()));
    log.info(String.format("Patch partitioning result by inside check:  %d/%d (incorrect/all)", incorrectByInsideCheck, patchesGraph.getNodes().values().size()));

    if(incorrectByVisibilityCheckCount * incorrectByInsideCheck == 0 && incorrectByVisibilityCheckCount + incorrectByInsideCheck != 0) {
      throw new RuntimeException("All validation approaches should have same result.");
    }

    return incorrectByVisibilityCheckCount == 0;
  }

  /**
   * Method test whether for each node of testing patch in its view range are present only nodes within this or neighbouring patches
   */
  private boolean testVisibilityCorrectness(Graph<PatchData, PatchConnectionData> patchesGraph,
      Graph<JunctionData, WayData> mapGraph,
      String patchId) {
    Node<PatchData, PatchConnectionData> patchNode = patchesGraph.getNodes().get(patchId);
    Set<String> neighbouringPatches =
        patchNode.getOutgoingEdges().stream().map(Edge::getTarget).map(Node::getId).collect(Collectors.toSet());

    Set<Node<JunctionData, WayData>> nodesToCheck = patchNode.getData()
        .getGraphInsidePatch()
        .getEdges()
        .values()
        .stream()
        .map(Edge::getTarget)
        .collect(Collectors.toSet());

    Set<String> visiblePatchIds = nodesToCheck.stream()
        .flatMap(n -> bfsWithRange.getInRange(mapGraph, n)
            .getEdgesInRange()
            .stream()
            .map(e -> e.getData().getPatchId()))
        .collect(Collectors.toSet());
    visiblePatchIds.remove(patchId);
    return neighbouringPatches.containsAll(visiblePatchIds);
  }

  /**
   * Method test whether each chord shorter than view range of current patch connect only the same or neighbouring patches
   */
  public boolean testInsideCorrectness(Graph<PatchData, PatchConnectionData> patchesGraph,
      Graph<JunctionData, WayData> mapGraph,
      String patchId) {
    Node<PatchData, PatchConnectionData> patchNode = patchesGraph.getNodes().get(patchId);

    Set<Node<JunctionData, WayData>> borderInNodes = patchNode.getData()
        .getGraphInsidePatch()
        .getEdges()
        .values()
        .stream()
        .flatMap(e -> Stream.of(e.getSource(), e.getTarget()))
        .filter(n -> isBorderInNode(n, patchId))
        .collect(Collectors.toSet());

    Set<Node<JunctionData, WayData>> borderOutNodes = patchNode.getData()
        .getGraphInsidePatch()
        .getEdges()
        .values()
        .stream()
        .flatMap(e -> Stream.of(e.getSource(), e.getTarget()))
        .filter(n -> isBorderOutNode(n, patchId))
        .collect(Collectors.toSet());

    ChordFinder chordFinder = new ChordFinder(patchNode);
    return borderInNodes.stream()
        .flatMap(sn -> chordFinder.findChord(sn, borderOutNodes).stream().map(tn -> new ImmutablePair<>(sn, tn)))
        .map(p -> checkNeighbouringBorderNodesPatches(p.getLeft(), p.getRight(), patchesGraph, patchId))
        .allMatch(b -> b);
  }

  private boolean checkNeighbouringBorderNodesPatches(
      Node<JunctionData, WayData> source,
      Node<JunctionData, WayData> target,
      Graph<PatchData, PatchConnectionData> patchesGraph,
      String patchIdToBeFiltered) {
    Set<String> sourcePatchesIds = source.getIncomingEdges().stream().map(e -> e.getData().getPatchId())
        .filter(patchId -> !patchId.equals(patchIdToBeFiltered))
        .collect(Collectors.toSet());
    Set<String> targetPatchesIds = target.getOutgoingEdges().stream().map(e -> e.getData().getPatchId())
        .filter(patchId -> !patchId.equals(patchIdToBeFiltered))
        .collect(Collectors.toSet());

    for (final String sourcePatchesId : sourcePatchesIds) {
      for (final String targetPatchesId : targetPatchesIds) {
        if (!sourcePatchesId.equals(targetPatchesId) && !areNeighbours(sourcePatchesId, targetPatchesId, patchesGraph)) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean areNeighbours(String patchId1, String patchId2, Graph<PatchData, PatchConnectionData> patchesGraph) {
    return patchesGraph.getNodes().get(patchId1).getOutgoingEdges().stream().map(Edge::getTarget).anyMatch(n -> n.getId().equals(patchId2));
  }

  private boolean isBorderInNode(Node<JunctionData, WayData> n, String relativePatchId) {
    return n.getIncomingEdges().stream().anyMatch(e -> !e.getData().getPatchId().equals(relativePatchId))
        && n.getOutgoingEdges().stream().anyMatch(e -> e.getData().getPatchId().equals(relativePatchId));
  }

  private boolean isBorderOutNode(Node<JunctionData, WayData> n, String relativePatchId) {
    return n.getOutgoingEdges().stream().anyMatch(e -> !e.getData().getPatchId().equals(relativePatchId))
        && n.getIncomingEdges().stream().anyMatch(e -> e.getData().getPatchId().equals(relativePatchId));
  }

  private class ChordFinder {

    private Graph<JunctionData, WayData> patch;

    public ChordFinder(Node<PatchData, PatchConnectionData> patchNode) {
      this.patch = patchNode.getData().getGraphInsidePatch();
    }

    public Set<Node<JunctionData, WayData>> findChord(Node<JunctionData, WayData> source, Set<Node<JunctionData, WayData>> targetCandidates) {
      Set<Edge<JunctionData, WayData>> edgesToMoveOn = new HashSet<>(patch.getEdges().values());
      BFSWithRangeResult<JunctionData, WayData> bfsWithRangeResult = bfsWithRange.getInRange(patch, source, edgesToMoveOn, true);
      Set<Node<JunctionData, WayData>> chordTargets = bfsWithRangeResult.getEdgesInRange()
          .stream()
          .map(Edge::getTarget) //this does not consider situation where real border of view range in os node
          .filter(targetCandidates::contains)
          .collect(Collectors.toSet());
      return chordTargets;
    }
  }

}
