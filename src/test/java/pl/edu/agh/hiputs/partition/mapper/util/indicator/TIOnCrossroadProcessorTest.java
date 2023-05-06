package pl.edu.agh.hiputs.partition.mapper.util.indicator;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class TIOnCrossroadProcessorTest {
  private final TIOnCrossroadProcessor processor = new TIOnCrossroadProcessor();

  @Test
  public void allocateOnBend() {
    // given
    Node<JunctionData, WayData> bend = new Node<>("0", JunctionData.builder().isCrossroad(false).build());

    // when
    processor.checkAndAllocate(bend);

    // then
    Assertions.assertTrue(bend.getData().getSignalsControlCenter().isEmpty());
  }

  @Test
  public void allocateOnCrossroad() {
    // given
    Node<JunctionData, WayData> bend = new Node<>("0", JunctionData.builder().isCrossroad(true).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());

    // when
    bend.getIncomingEdges().addAll(List.of(edge1, edge2));
    processor.checkAndAllocate(bend);

    // then
    Assertions.assertTrue(bend.getData().getSignalsControlCenter().isPresent());
    Assertions.assertTrue(edge1.getData().getTrafficIndicator().isPresent());
    Assertions.assertTrue(edge2.getData().getTrafficIndicator().isPresent());
  }
}
