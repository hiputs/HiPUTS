package pl.edu.agh.hiputs.partition.mapper.util.successor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

public class DefaultPairingIncomingWithOutgoings {

  public void pair(WayData incomingEdge, List<Edge<JunctionData, WayData>> outgoingEdges) {
    if (outgoingEdges.size() >= incomingEdge.getLanes().size()) {
      pairLanesGroupByGroup(
          incomingEdge,
          new ArrayList<>(generateNoGroups(outgoingEdges.size(), incomingEdge.getLanes().size())),
          (i) -> outgoingEdges.get(i).getData().getLanes(),
          false
      );
    }
    else {
      List<LaneData> outgoingLanes = outgoingEdges.stream()
          .flatMap(edge -> edge.getData().getLanes().stream())
          .toList();

      if (outgoingLanes.size() >= incomingEdge.getLanes().size()) {
        pairLanesGroupByGroup(
            incomingEdge,
            new ArrayList<>(generateNoGroups(outgoingLanes.size(), incomingEdge.getLanes().size())),
            (i) -> new ArrayList<>(){{add(outgoingLanes.get(i));}},
            false
        );
      }
      else {
        pairLanesGroupByGroup(
            incomingEdge,
            new ArrayList<>(generateNoGroups(incomingEdge.getLanes().size(), outgoingLanes.size())),
            (i) -> new ArrayList<>(){{ add(outgoingLanes.get(i)); }},
            true
        );
      }
    }
  }

  private List<Integer> generateNoGroups(int entriesNo, int groupsNo) {
    AtomicInteger entriesLeft = new AtomicInteger(entriesNo);

    return IntStream.iterate(groupsNo, i -> i - 1)
        .limit(groupsNo)
        .mapToObj(group -> entriesLeft.getAndUpdate(entries -> entries - entries / group) / group)
        .toList();
  }

  private void pairLanesGroupByGroup(
      WayData incoming,
      List<Integer> steps,
      Function<Integer, List<LaneData>> lanesGetter,
      boolean iterOverIncoming
  ) {
    Collections.reverse(steps);
    AtomicInteger startEdgeIndex = new AtomicInteger(0);

    // @TODO consider left-hand traffic
    IntStream.range(0, steps.size())
        .forEach(stepIndex -> IntStream.iterate(startEdgeIndex.getAndUpdate(i -> i + steps.get(stepIndex)), i -> i + 1)
            .limit(steps.get(stepIndex))
            .forEach(edgeIndex ->
                incoming.getLanes().get(iterOverIncoming ? edgeIndex : stepIndex).getAvailableSuccessors()
                    .addAll(lanesGetter.apply(iterOverIncoming ? stepIndex : edgeIndex))));
  }
}
