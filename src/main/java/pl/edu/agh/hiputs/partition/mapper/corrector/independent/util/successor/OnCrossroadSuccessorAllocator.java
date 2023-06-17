package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.PairingIncomingWithOutgoings;
import pl.edu.agh.hiputs.partition.mapper.util.sort.EdgeSorter;
import pl.edu.agh.hiputs.partition.mapper.util.turn.TurnDirection;
import pl.edu.agh.hiputs.partition.mapper.util.turn.mapper.TurnMapper;
import pl.edu.agh.hiputs.partition.mapper.util.turn.processor.TurnProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(2)
@RequiredArgsConstructor
public class OnCrossroadSuccessorAllocator implements SuccessorAllocator{
  private final PairingIncomingWithOutgoings defaultAllocator;
  private final TurnProcessor turnProcessor;
  private final TurnMapper turnMapper;
  private final EdgeSorter edgeSorter;

  @Override
  public void allocateOnNode(Node<JunctionData, WayData> node) {
    if (node.getData().isCrossroad()) {
      allocateOnCrossroad(node);
    }
  }

  private void allocateOnCrossroad(Node<JunctionData, WayData> crossroad) {
    crossroad.getIncomingEdges().forEach(incomingEdge -> {
      List<Edge<JunctionData, WayData>> sortedOutgoings = edgeSorter.getSorted(
          crossroad.getOutgoingEdges(), incomingEdge, edge -> edge.getTarget().getData());
      List<List<TurnDirection>> availableTurns = turnProcessor.getTurnDirectionsFromTags(incomingEdge.getData());

      Optional.of(availableTurns)
          .filter(turnsList -> !turnsList.isEmpty() && turnsList.size() == incomingEdge.getData().getLanes().size())
          .ifPresentOrElse(turnsList -> {
            // typical zip for lists of turns and lanes
            List<Pair<LaneData, List<TurnDirection>>> zippedLanesAndTurns = IntStream.range(0, turnsList.size())
                .mapToObj(index -> Pair.of(incomingEdge.getData().getLanes().get(index), turnsList.get(index)))
                .toList();

            Map<TurnDirection, Edge<JunctionData, WayData>> turn2OutgoingEdge = turnMapper.assignTurns2OutgoingEdges(
                sortedOutgoings.subList(1, sortedOutgoings.size()), incomingEdge
            );

            if (zippedLanesAndTurns.stream()
                .filter(pair -> !pair.getRight().contains(TurnDirection.REVERSE))
                .allMatch(pair -> pair.getRight().stream()
                    .allMatch(turn2OutgoingEdge::containsKey))) {
              // allocating successors when turn->outgoings data are complete
              zippedLanesAndTurns.stream()
                  .filter(pair -> !pair.getRight().contains(TurnDirection.REVERSE))
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
    });
  }
}
