package pl.edu.agh.hiputs.partition.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.util.turn.TurnDirection;
import pl.edu.agh.hiputs.partition.mapper.util.turn.TurnProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(7)
@RequiredArgsConstructor
public class GraphNextLanesAllocator implements GraphTransformer {
  private final TurnProcessor turnProcessor;

  @Override
  public Graph<JunctionData, WayData> transform(Graph<JunctionData, WayData> graph) {
    graph.getNodes().values().stream()
        .filter(node -> node.getData().isCrossroad())
        .forEach(this::allocateSuccessorsOnCrossroad);

    graph.getNodes().values().stream()
        .filter(node -> !node.getData().isCrossroad())
        .forEach(this::allocateSuccessorsOnBend);

    return graph;
  }

  private void allocateSuccessorsOnCrossroad(Node<JunctionData, WayData> crossroad) {
    crossroad.getIncomingEdges().forEach(incomingEdge -> {
      List<List<TurnDirection>> availableTurns = turnProcessor.getTurnDirectionsFromTags(incomingEdge.getData());
      Optional.of(availableTurns)
          .filter(list -> !list.isEmpty())
          .ifPresentOrElse(list -> {
            Set<TurnDirection> distinctTurns = list.stream()
                .flatMap(List::stream)
                .sorted()
                .collect(Collectors.toCollection(TreeSet::new));
            if (distinctTurns.size() != crossroad.getOutgoingEdges().size()) {
            }
            // @TODO allocating successors to crossroads with turn directions in tags
          }, () -> {
            // @TODO allocating successors to crossroads without turn directions in tags
          });
    });
  }

  // merge lanes also included here
  // iteracja po incomingach, filtrowanie outgoingów
  private void allocateSuccessorsOnBend(Node<JunctionData, WayData> bend) {
    if (bend.getOutgoingEdges().size() == 1) {
      // pairing all incoming edges with this one outgoing edge
      bend.getIncomingEdges().forEach(incoming ->
          pairRoadsWithBend(incoming.getData(), bend.getOutgoingEdges().get(0).getData()));
    }
    else if (bend.getOutgoingEdges().size() == 2) {
      // pairing all incoming edges only with these outgoing, which are not a reverse
      bend.getIncomingEdges().forEach(incoming ->
          bend.getOutgoingEdges().stream()
              .filter(outgoing -> !outgoing.getTarget().equals(incoming.getSource()))
              .forEach(outgoing -> pairRoadsWithBend(incoming.getData(), outgoing.getData())));
    }
  }

  private void pairRoadsWithBend(WayData incoming, WayData outgoing) {
    List<List<TurnDirection>> lanesDirections = turnProcessor.getTurnDirectionsFromTags(incoming);

    // removing merge lanes at incoming edge if they are present in tags
    List<LaneData> incomingLanes = new ArrayList<>(incoming.getLanes());
    incomingLanes.removeAll(
        IntStream.range(0, lanesDirections.size())
            .filter(index -> lanesDirections.get(index).contains(TurnDirection.MERGE_TO_RIGHT) ||
            lanesDirections.get(index).contains(TurnDirection.MERGE_TO_LEFT))
            .mapToObj(incomingLanes::get)
            .toList()
    );

    int minNoLanes = Math.min(incomingLanes.size(), outgoing.getLanes().size());

    int incomingDiff = incomingLanes.size() - minNoLanes;
    int incomingLowerOffset = incomingDiff / 2;

    int outgoingDiff = outgoing.getLanes().size() - minNoLanes;
    int outgoingLowerOffset = outgoingDiff / 2;
    int outgoingUpperOffset = outgoingDiff - outgoingLowerOffset;

    // mapping center incoming lanes to center outgoing lanes
    IntStream.range(0, minNoLanes).forEach(index ->
        incomingLanes.get(index + incomingLowerOffset).getAvailableSuccessors().add(
            outgoing.getLanes().get(index + outgoingLowerOffset))
    );

    // appending border left outgoing lanes (if they exist after bend) to border left incoming lane
    IntStream.range(0, outgoingLowerOffset).forEach(index ->
        incomingLanes.get(incomingLowerOffset).getAvailableSuccessors().add(
            outgoing.getLanes().get(index))
    );

    // appending border right outgoing lanes (if they exist after bend) to border right incoming lane
    IntStream.range(0, outgoingUpperOffset).forEach(index ->
        incomingLanes.get(incomingLowerOffset + minNoLanes - 1).getAvailableSuccessors().add(
            outgoing.getLanes().get(index + outgoingLowerOffset + minNoLanes))
    );
  }
}
