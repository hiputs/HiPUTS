package pl.edu.agh.hiputs.partition.mapper.util.successor.pairing;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@Service
public class DefaultPairingIncomingWithOutgoings implements PairingIncomingWithOutgoings{

  @Override
  public void pair(WayData incomingEdge, List<Edge<JunctionData, WayData>> outgoingEdges) {
    if (outgoingEdges.size() >= incomingEdge.getLanes().size()) {
      pairLanesGroupByGroup(
          incomingEdge,
          generateNoGroups(outgoingEdges.size(), incomingEdge.getLanes().size()),
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
            generateNoGroups(outgoingLanes.size(), incomingEdge.getLanes().size()),
            (i) -> Stream.of(outgoingLanes.get(i)).collect(Collectors.toList()),
            false
        );
      }
      else {
        pairLanesGroupByGroup(
            incomingEdge,
            generateNoGroups(incomingEdge.getLanes().size(), outgoingLanes.size()),
            (i) -> Stream.of(outgoingLanes.get(i)).collect(Collectors.toList()),
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
        .collect(Collectors.toList());
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
        .forEach(stepIndex -> IntStream.range(
                startEdgeIndex.get(),
                startEdgeIndex.getAndUpdate(i -> i + steps.get(stepIndex)) + steps.get(stepIndex))
            .forEach(edgeIndex ->
                incoming.getLanes().get(iterOverIncoming ? edgeIndex : stepIndex).getAvailableSuccessors()
                    .addAll(lanesGetter.apply(iterOverIncoming ? stepIndex : edgeIndex))));
  }
}
