package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.pairing.PairingIncomingWithOutgoings;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.sort.EdgeSorter;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.TurnDirection;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.mapper.TurnMapper;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.processor.TurnProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.partition.model.relation.Restriction;

@Service
@Order(2)
@RequiredArgsConstructor
public class OnCrossroadSuccessorAllocator implements SuccessorAllocator, RestrictionAware {
  private final PairingIncomingWithOutgoings defaultAllocator;
  private final TurnProcessor turnProcessor;
  private final TurnMapper turnMapper;
  private final EdgeSorter edgeSorter;

  private Map<String, Set<Restriction>> fromEdgeId2Restrictions = new HashMap<>();

  @Override
  public void provideRestrictions(Set<Restriction> restrictions) {
    this.fromEdgeId2Restrictions = restrictions.stream()
        .collect(Collectors.groupingBy(Restriction::getFromEdgeId, Collectors.toSet()));

    this.fromEdgeId2Restrictions.keySet().forEach(fromEdgeId -> {
      List<Restriction> foundOnlyRestrictions = this.fromEdgeId2Restrictions.get(fromEdgeId).stream()
          .filter(restriction ->
              Objects.nonNull(restriction.getType()) && restriction.getType().toString().startsWith("ONLY_"))
          .toList();

      if (foundOnlyRestrictions.size() > 1) {
        foundOnlyRestrictions.forEach(this.fromEdgeId2Restrictions.get(fromEdgeId)::remove);
      }
    });
  }

  @Override
  public void allocateOnNode(Node<JunctionData, WayData> node) {
    if (node.getData().isCrossroad()) {
      allocateOnCrossroad(node);
    }
  }

  private void allocateOnCrossroad(Node<JunctionData, WayData> crossroad) {
    crossroad.getIncomingEdges().forEach(incomingEdge -> {
      List<Edge<JunctionData, WayData>> allSortedOutgoings = edgeSorter.getSorted(
          crossroad.getOutgoingEdges(),
          createReversedRefEdge(incomingEdge),
          edge -> edge.getTarget().getData(),
          edge -> edge.getSource().getData());

      Optional.of(allSortedOutgoings)
          .map(sortedOutgoings -> getAllowedRoads(incomingEdge, sortedOutgoings))
          .filter(sortedOutgoings -> !sortedOutgoings.isEmpty())
          .ifPresentOrElse(sortedOutgoings -> {
            List<List<TurnDirection>> availableTurns = turnProcessor.getTurnDirectionsFromTags(incomingEdge.getData());

            Optional.of(availableTurns)
                .filter(turnsList -> !turnsList.isEmpty() && turnsList.size() == incomingEdge.getData().getLanes().size())
                .ifPresentOrElse(turnsList -> {
                  // typical zip for lists of turns and lanes
                  List<Pair<LaneData, List<TurnDirection>>> zippedLanesAndTurns = IntStream.range(0, turnsList.size())
                      .mapToObj(index -> Pair.of(incomingEdge.getData().getLanes().get(index), turnsList.get(index)))
                      .toList();

                  // checking if reversing is aborted
                  boolean reverseAborted = turnsList.stream()
                      .allMatch(turnDirections -> turnDirections.stream()
                          .noneMatch(turnDirection -> turnDirection == TurnDirection.REVERSE));

                  // getting all exit options by turns
                  Map<TurnDirection, Edge<JunctionData, WayData>> turn2OutgoingEdge = turnMapper.assignTurns2OutgoingEdges(
                      reverseAborted ? sortedOutgoings : sortedOutgoings.subList(1, sortedOutgoings.size()), incomingEdge
                  );

                  if (zippedLanesAndTurns.stream()
                      .filter(pair -> !pair.getRight().contains(TurnDirection.REVERSE))
                      .allMatch(pair -> pair.getRight().stream()
                          .allMatch(turn2OutgoingEdge::containsKey))) {
                    // allocating successors when turn->outgoings data are complete
                    zippedLanesAndTurns.stream()
                        .filter(pair ->  reverseAborted || !pair.getRight().contains(TurnDirection.REVERSE))
                        .forEach(pair -> pair.getLeft().getAvailableSuccessors().addAll(pair.getRight().stream()
                            .map(turn2OutgoingEdge::get)
                            .flatMap(edge -> edge.getData().getLanes().stream())
                            .toList()));

                    // assigning border left road lanes to incoming lane with reverse turn
                    zippedLanesAndTurns.stream()
                        .filter(pair -> pair.getRight().contains(TurnDirection.REVERSE))
                        .map(Pair::getLeft)
                        .forEach(incomingLane ->
                            // @TODO consider left-hand traffic
                            incomingLane.getAvailableSuccessors().addAll(sortedOutgoings.get(0).getData().getLanes()));
                  }
                  else {
                    // otherwise delegating to default provider
                    defaultAllocator.pair(incomingEdge.getData(), sortedOutgoings);
                  }
                }, () -> defaultAllocator.pair(incomingEdge.getData(), sortedOutgoings));
          }, () -> defaultAllocator.pair(incomingEdge.getData(), allSortedOutgoings));
    });
  }

  private Edge<JunctionData, WayData> createReversedRefEdge(Edge<JunctionData, WayData> refEdge){
    Edge<JunctionData, WayData> reversedRefEdge = new Edge<>("", WayData.builder().build());
    reversedRefEdge.setSource(refEdge.getTarget());
    reversedRefEdge.setTarget(refEdge.getSource());

    return reversedRefEdge;
  }

  private List<Edge<JunctionData, WayData>> getAllowedRoads(
      Edge<JunctionData, WayData> incomingEdge, List<Edge<JunctionData, WayData>> outgoingEdges
  ) {
    Set<String> edgesIdsToContain = fromEdgeId2Restrictions.getOrDefault(incomingEdge.getId(), Collections.emptySet())
        .stream()
        .filter(restriction -> restriction.getViaNodeId().equals(incomingEdge.getTarget().getId()))
        .filter(restriction -> restriction.getType().toString().startsWith("ONLY_"))
        .map(Restriction::getToEdgeId)
        .collect(Collectors.toSet());

    Set<String> edgesIdsToRemove = fromEdgeId2Restrictions.getOrDefault(incomingEdge.getId(), Collections.emptySet())
        .stream()
        .filter(restriction -> restriction.getViaNodeId().equals(incomingEdge.getTarget().getId()))
        .filter(restriction -> restriction.getType().toString().startsWith("NO_"))
        .map(Restriction::getToEdgeId)
        .collect(Collectors.toSet());

    return outgoingEdges.stream()
        .filter(edge -> edgesIdsToContain.isEmpty() || edgesIdsToContain.contains(edge.getId()))
        .filter(edge -> !edgesIdsToRemove.contains(edge.getId()))
        .collect(Collectors.toList());
  }
}
