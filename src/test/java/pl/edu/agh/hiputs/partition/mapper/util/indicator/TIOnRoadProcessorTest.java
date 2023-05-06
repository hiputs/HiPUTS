package pl.edu.agh.hiputs.partition.mapper.util.indicator;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class TIOnRoadProcessorTest {
  private final TIOnRoadProcessor processor = new TIOnRoadProcessor(
      new StandardTIDeterminer(), new TIOnCrossroadProcessor()
  );

  @Test
  public void allocateWhenCrossroad() {
    // given
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().tags(Map.of()).isCrossroad(true).build());

    // when
    processor.checkAndAllocate(crossroad);

    // then
    Assertions.assertTrue(crossroad.getData().getSignalsControlCenter().isEmpty());
  }

  @Test
  public void allocateWhenBend() {
    // given
    Node<JunctionData, WayData> bend = new Node<>("0",
        JunctionData.builder().tags(Map.of()).isCrossroad(false).build());
    Node<JunctionData, WayData> crossroad = new Node<>("1",
        JunctionData.builder().tags(Map.of()).isCrossroad(true).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("01", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("10", WayData.builder().build());

    // when
    bend.getIncomingEdges().add(edge2);
    bend.getOutgoingEdges().add(edge1);
    crossroad.getIncomingEdges().add(edge1);
    crossroad.getOutgoingEdges().add(edge2);
    edge1.setSource(bend);
    edge1.setTarget(crossroad);
    edge2.setSource(crossroad);
    edge2.setTarget(bend);
    processor.checkAndAllocate(bend);

    // then
    Assertions.assertTrue(bend.getData().getSignalsControlCenter().isEmpty());
    Assertions.assertTrue(crossroad.getData().getSignalsControlCenter().isPresent());
    Assertions.assertTrue(edge1.getData().getTrafficIndicator().isPresent());
    Assertions.assertTrue(edge2.getData().getTrafficIndicator().isEmpty());
  }

  @Test
  public void allocateWhenBendAndMoreCrossroadsNearby() {
    // given
    Node<JunctionData, WayData> bend = new Node<>("0",
        JunctionData.builder().tags(Map.of()).isCrossroad(false).build());
    Node<JunctionData, WayData> crossroad1 = new Node<>("1",
        JunctionData.builder().tags(Map.of()).isCrossroad(true).build());
    Node<JunctionData, WayData> crossroad2 = new Node<>("2",
        JunctionData.builder().tags(Map.of()).isCrossroad(true).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("01", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("10", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("02", WayData.builder().build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("20", WayData.builder().build());

    // when
    bend.getIncomingEdges().addAll(List.of(edge2, edge4));
    bend.getOutgoingEdges().addAll(List.of(edge1, edge3));
    crossroad1.getIncomingEdges().add(edge1);
    crossroad1.getOutgoingEdges().add(edge2);
    crossroad2.getIncomingEdges().add(edge3);
    crossroad2.getOutgoingEdges().add(edge4);
    edge1.setSource(bend);
    edge1.setTarget(crossroad1);
    edge2.setSource(crossroad1);
    edge2.setTarget(bend);
    edge3.setSource(bend);
    edge3.setTarget(crossroad2);
    edge4.setSource(crossroad2);
    edge4.setTarget(bend);
    processor.checkAndAllocate(bend);

    // then
    Assertions.assertTrue(bend.getData().getSignalsControlCenter().isEmpty());
    Assertions.assertTrue(crossroad1.getData().getSignalsControlCenter().isPresent());
    Assertions.assertTrue(crossroad2.getData().getSignalsControlCenter().isEmpty());
    Assertions.assertTrue(edge1.getData().getTrafficIndicator().isPresent());
    Assertions.assertTrue(edge2.getData().getTrafficIndicator().isEmpty());
    Assertions.assertTrue(edge3.getData().getTrafficIndicator().isEmpty());
    Assertions.assertTrue(edge4.getData().getTrafficIndicator().isEmpty());
  }

  @Test
  public void allocateWhenBendAndOneTIFreeCrossroadNearby() {
    // given
    Node<JunctionData, WayData> bend = new Node<>("0",
        JunctionData.builder().tags(Map.of()).isCrossroad(false).build());
    Node<JunctionData, WayData> crossroad1 = new Node<>("1",
        JunctionData.builder().tags(Map.of("highway", "traffic_signals")).isCrossroad(true).build());
    Node<JunctionData, WayData> crossroad2 = new Node<>("2",
        JunctionData.builder().tags(Map.of()).isCrossroad(true).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("01", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("10", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("02", WayData.builder().build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("20", WayData.builder().build());

    // when
    bend.getIncomingEdges().addAll(List.of(edge2, edge4));
    bend.getOutgoingEdges().addAll(List.of(edge1, edge3));
    crossroad1.getIncomingEdges().add(edge1);
    crossroad1.getOutgoingEdges().add(edge2);
    crossroad2.getIncomingEdges().add(edge3);
    crossroad2.getOutgoingEdges().add(edge4);
    edge1.setSource(bend);
    edge1.setTarget(crossroad1);
    edge2.setSource(crossroad1);
    edge2.setTarget(bend);
    edge3.setSource(bend);
    edge3.setTarget(crossroad2);
    edge4.setSource(crossroad2);
    edge4.setTarget(bend);
    processor.checkAndAllocate(bend);

    // then
    Assertions.assertTrue(bend.getData().getSignalsControlCenter().isEmpty());
    Assertions.assertTrue(crossroad1.getData().getSignalsControlCenter().isEmpty());
    Assertions.assertTrue(crossroad2.getData().getSignalsControlCenter().isPresent());
    Assertions.assertTrue(edge1.getData().getTrafficIndicator().isEmpty());
    Assertions.assertTrue(edge2.getData().getTrafficIndicator().isEmpty());
    Assertions.assertTrue(edge3.getData().getTrafficIndicator().isPresent());
    Assertions.assertTrue(edge4.getData().getTrafficIndicator().isEmpty());
  }

  @Test
  public void allocateWhenBendAndNoTIFreeCrossroadNearby() {
    // given
    Node<JunctionData, WayData> bend = new Node<>("0",
        JunctionData.builder().tags(Map.of()).isCrossroad(false).build());
    Node<JunctionData, WayData> crossroad1 = new Node<>("1",
        JunctionData.builder().tags(Map.of("highway", "traffic_signals")).isCrossroad(true).build());
    Node<JunctionData, WayData> crossroad2 = new Node<>("2",
        JunctionData.builder().tags(Map.of("highway", "traffic_signals")).isCrossroad(true).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("01", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("10", WayData.builder().build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("02", WayData.builder().build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("20", WayData.builder().build());

    // when
    bend.getIncomingEdges().addAll(List.of(edge2, edge4));
    bend.getOutgoingEdges().addAll(List.of(edge1, edge3));
    crossroad1.getIncomingEdges().add(edge1);
    crossroad1.getOutgoingEdges().add(edge2);
    crossroad2.getIncomingEdges().add(edge3);
    crossroad2.getOutgoingEdges().add(edge4);
    edge1.setSource(bend);
    edge1.setTarget(crossroad1);
    edge2.setSource(crossroad1);
    edge2.setTarget(bend);
    edge3.setSource(bend);
    edge3.setTarget(crossroad2);
    edge4.setSource(crossroad2);
    edge4.setTarget(bend);
    processor.checkAndAllocate(bend);

    // then
    Assertions.assertTrue(bend.getData().getSignalsControlCenter().isEmpty());
    Assertions.assertTrue(crossroad1.getData().getSignalsControlCenter().isEmpty());
    Assertions.assertTrue(crossroad2.getData().getSignalsControlCenter().isEmpty());
    Assertions.assertTrue(edge1.getData().getTrafficIndicator().isEmpty());
    Assertions.assertTrue(edge2.getData().getTrafficIndicator().isEmpty());
    Assertions.assertTrue(edge3.getData().getTrafficIndicator().isEmpty());
    Assertions.assertTrue(edge4.getData().getTrafficIndicator().isEmpty());
  }
}
