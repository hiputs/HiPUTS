package pl.edu.agh.hiputs.partition.mapper.util.successor.allocator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.util.successor.pairing.PairingIncomingWithOutgoings;
import pl.edu.agh.hiputs.partition.mapper.util.turn.TurnDirection;
import pl.edu.agh.hiputs.partition.mapper.util.turn.mapper.TurnMapper;
import pl.edu.agh.hiputs.partition.mapper.util.turn.processor.TurnProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.geom.ClockwiseSorting;
import pl.edu.agh.hiputs.partition.model.geom.Point;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(2)
@RequiredArgsConstructor
public class OnCrossroadSuccessorAllocator implements SuccessorAllocator{
  // @TODO consider left-hand traffic
  private final ClockwiseSorting<Edge<JunctionData, WayData>> edgeSorter = new ClockwiseSorting<>(false);
  private final PairingIncomingWithOutgoings defaultAllocator;
  private final TurnProcessor turnProcessor;
  private final TurnMapper turnMapper;

  @Override
  public void allocateOnNode(Node<JunctionData, WayData> node) {
    if (node.getData().isCrossroad()) {
      allocateOnCrossroad(node);
    }
  }

  private void allocateOnCrossroad(Node<JunctionData, WayData> crossroad) {
    crossroad.getIncomingEdges().forEach(incomingEdge -> {
      List<Edge<JunctionData, WayData>> sortedOutgoings = getSortedOutgoings(crossroad.getOutgoingEdges(), incomingEdge);
      List<List<TurnDirection>> availableTurns = turnProcessor.getTurnDirectionsFromTags(incomingEdge.getData());

      Optional.of(availableTurns)
          .filter(turnsList -> !turnsList.isEmpty() && turnsList.size() == incomingEdge.getData().getLanes().size())
          .ifPresentOrElse(turnsList -> {
            // typical zip for lists of turns and lanes
            List<Pair<LaneData, List<TurnDirection>>> zippedLanesAndTurns = IntStream.range(0, turnsList.size())
                .mapToObj(index -> Pair.of(incomingEdge.getData().getLanes().get(index), turnsList.get(index)))
                .toList();

            Map<TurnDirection, Edge<JunctionData, WayData>> turn2OutgoingEdge = turnMapper.assignTurns2OutgoingEdges(
                sortedOutgoings, incomingEdge
            );

            if (zippedLanesAndTurns.stream()
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

  private List<Edge<JunctionData, WayData>> getSortedOutgoings(
      List<Edge<JunctionData, WayData>> outgoings,
      Edge<JunctionData, WayData> incoming
  ) {
    List<Pair<Point, Edge<JunctionData, WayData>>> dataToSort = outgoings.stream()
        .map(edge -> Pair.of(Point.convertFromCoords(edge.getTarget().getData()), edge))
        .collect(Collectors.toList());

    edgeSorter.sortByPointsWithRef(
        dataToSort,
        Point.convertFromCoords(incoming.getTarget().getData()),
        Point.convertFromCoords(incoming.getSource().getData())
    );

    return dataToSort.stream()
        .map(Pair::getRight)
        .collect(Collectors.toList());
  }
}
