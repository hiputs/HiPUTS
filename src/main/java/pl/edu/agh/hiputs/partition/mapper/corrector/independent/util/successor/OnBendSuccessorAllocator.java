package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.util.turn.TurnDirection;
import pl.edu.agh.hiputs.partition.mapper.util.turn.processor.TurnProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@Service
@Order(1)
@RequiredArgsConstructor
public class OnBendSuccessorAllocator implements SuccessorAllocator{
  private final TurnProcessor turnProcessor;

  @Override
  public void allocateOnNode(Node<JunctionData, WayData> node) {
    if (!node.getData().isCrossroad()) {
      allocateOnBend(node);
    }
  }

  private void allocateOnBend(Node<JunctionData, WayData> bend) {
    if (bend.getOutgoingEdges().size() == 1) {
      // pairing all incoming edges with this one outgoing edge
      bend.getIncomingEdges().forEach(incomingEdge ->
          pairRoads(incomingEdge.getData(), bend.getOutgoingEdges().get(0).getData()));
    }
    else if (bend.getOutgoingEdges().size() == 2) {
      // pairing all incoming edges only with these outgoing, which are not a reverse
      bend.getIncomingEdges().forEach(incomingEdge ->
          bend.getOutgoingEdges().stream()
              .filter(outgoingEdge -> !outgoingEdge.getTarget().equals(incomingEdge.getSource()))
              .forEach(outgoingEdge -> pairRoads(incomingEdge.getData(), outgoingEdge.getData())));
    }
  }

  private void pairRoads(WayData incoming, WayData outgoing) {
    List<List<TurnDirection>> turnDirectionsOnLanes = turnProcessor.getTurnDirectionsFromTags(incoming);

    // removing merge lanes from considering if they are present in tags and the next way has different lanes number
    List<LaneData> incomingLanes = new ArrayList<>(incoming.getLanes());
    if (incomingLanes.size() != outgoing.getLanes().size()) {
      incomingLanes.removeAll(
          IntStream.range(0, turnDirectionsOnLanes.size())
              .filter(index -> turnDirectionsOnLanes.get(index).contains(TurnDirection.MERGE_TO_RIGHT) ||
                  turnDirectionsOnLanes.get(index).contains(TurnDirection.MERGE_TO_LEFT))
              .mapToObj(incomingLanes::get)
              .toList()
      );
    }

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
