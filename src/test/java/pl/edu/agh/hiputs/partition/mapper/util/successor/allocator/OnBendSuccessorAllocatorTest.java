package pl.edu.agh.hiputs.partition.mapper.util.successor.allocator;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.util.turn.processor.StandardOsmTurnProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class OnBendSuccessorAllocatorTest {
  private final OnBendSuccessorAllocator allocator = new OnBendSuccessorAllocator(
      new StandardOsmTurnProcessor()
  );

  @Test
  public void bendWithOneOutgoingEdge() {
    // given
    LaneData inLane1 = LaneData.builder().build();
    LaneData inLane2 = LaneData.builder().build();
    LaneData outLane = LaneData.builder().build();
    Node<JunctionData, WayData> crossroad = new Node<>("0", JunctionData.builder().isCrossroad(false).build());
    Edge<JunctionData, WayData> inEdge1 = new Edge<>("1", WayData.builder().lanes(List.of(inLane1)).tags(Collections.emptyMap()).build());
    Edge<JunctionData, WayData> inEdge2 = new Edge<>("2", WayData.builder().lanes(List.of(inLane2)).tags(Collections.emptyMap()).build());
    Edge<JunctionData, WayData> outEdge = new Edge<>("3", WayData.builder().lanes(List.of(outLane)).build());

    // when
    inEdge1.setSource(Mockito.mock(Node.class));
    inEdge1.setTarget(crossroad);
    inEdge2.setSource(Mockito.mock(Node.class));
    inEdge2.setTarget(crossroad);
    outEdge.setSource(crossroad);
    outEdge.setTarget(Mockito.mock(Node.class));
    crossroad.getIncomingEdges().addAll(List.of(inEdge1, inEdge2));
    crossroad.getOutgoingEdges().add(outEdge);
    allocator.allocateOnNode(crossroad);

    // then
    Assertions.assertEquals(outLane, inLane1.getAvailableSuccessors().get(0));
    Assertions.assertEquals(outLane, inLane2.getAvailableSuccessors().get(0));
  }

  @Test
  public void bendWithTwoOutgoingEdges() {
    // given
    LaneData inLane1 = LaneData.builder().build();
    LaneData inLane2 = LaneData.builder().build();
    LaneData outLane1 = LaneData.builder().build();
    LaneData outLane2 = LaneData.builder().build();
    Node<JunctionData, WayData> crossroad = new Node<>("00", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> node1 = new Node<>("01", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> node2 = new Node<>("02", JunctionData.builder().isCrossroad(false).build());
    Edge<JunctionData, WayData> inEdge1 = new Edge<>("1", WayData.builder().lanes(List.of(inLane1)).tags(Collections.emptyMap()).build());
    Edge<JunctionData, WayData> inEdge2 = new Edge<>("2", WayData.builder().lanes(List.of(inLane2)).tags(Collections.emptyMap()).build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("3", WayData.builder().lanes(List.of(outLane1)).build());
    Edge<JunctionData, WayData> outEdge2 = new Edge<>("4", WayData.builder().lanes(List.of(outLane2)).build());

    // when
    inEdge1.setSource(node1);
    inEdge1.setTarget(crossroad);
    inEdge2.setSource(node2);
    inEdge2.setTarget(crossroad);
    outEdge1.setSource(crossroad);
    outEdge1.setTarget(node1);
    outEdge2.setSource(crossroad);
    outEdge2.setTarget(node2);
    crossroad.getIncomingEdges().addAll(List.of(inEdge1, inEdge2));
    crossroad.getOutgoingEdges().addAll(List.of(outEdge1, outEdge2));
    allocator.allocateOnNode(crossroad);

    // then
    Assertions.assertEquals(outLane2, inLane1.getAvailableSuccessors().get(0));
    Assertions.assertEquals(outLane1, inLane2.getAvailableSuccessors().get(0));
  }

  @Test
  public void bendMoreLanesWithOneLane() {
    // given
    LaneData inLane1 = LaneData.builder().build();
    LaneData inLane2 = LaneData.builder().build();
    LaneData inLane3 = LaneData.builder().build();
    LaneData outLane = LaneData.builder().build();
    Node<JunctionData, WayData> crossroad = new Node<>("0", JunctionData.builder().isCrossroad(false).build());
    Edge<JunctionData, WayData> inEdge1 = new Edge<>("1", WayData.builder()
        .lanes(List.of(inLane1, inLane2, inLane3))
        .tags(Collections.emptyMap())
        .build());
    Edge<JunctionData, WayData> outEdge = new Edge<>("2", WayData.builder().lanes(List.of(outLane)).build());

    // when
    inEdge1.setSource(Mockito.mock(Node.class));
    inEdge1.setTarget(crossroad);
    outEdge.setSource(crossroad);
    outEdge.setTarget(Mockito.mock(Node.class));
    crossroad.getIncomingEdges().add(inEdge1);
    crossroad.getOutgoingEdges().add(outEdge);
    allocator.allocateOnNode(crossroad);

    // then
    Assertions.assertTrue(inLane1.getAvailableSuccessors().isEmpty());
    Assertions.assertEquals(outLane, inLane2.getAvailableSuccessors().get(0));
    Assertions.assertTrue(inLane3.getAvailableSuccessors().isEmpty());
  }

  @Test
  public void bendMoreLanesToMoreLanesWithMergeDeletion() {
    // given
    LaneData inLane1 = LaneData.builder().build();
    LaneData inLane2 = LaneData.builder().build();
    LaneData outLane1 = LaneData.builder().build();
    Node<JunctionData, WayData> crossroad = new Node<>("0", JunctionData.builder().isCrossroad(false).build());
    Edge<JunctionData, WayData> inEdge = new Edge<>("1", WayData.builder()
        .lanes(List.of(inLane1, inLane2))
        .tags(Collections.singletonMap("turn:lanes", "none|merge_to_left"))
        .build());
    Edge<JunctionData, WayData> outEdge = new Edge<>("2", WayData.builder()
        .lanes(List.of(outLane1))
        .build());

    // when
    inEdge.setSource(Mockito.mock(Node.class));
    inEdge.setTarget(crossroad);
    outEdge.setSource(crossroad);
    outEdge.setTarget(Mockito.mock(Node.class));
    crossroad.getIncomingEdges().add(inEdge);
    crossroad.getOutgoingEdges().add(outEdge);
    allocator.allocateOnNode(crossroad);

    // then
    Assertions.assertEquals(List.of(outLane1), inLane1.getAvailableSuccessors());
    Assertions.assertTrue(inLane2.getAvailableSuccessors().isEmpty());
  }

  @Test
  public void bendMoreLanesToMoreLanesWithoutMergeDeletion() {
    // given
    LaneData inLane1 = LaneData.builder().build();
    LaneData inLane2 = LaneData.builder().build();
    LaneData outLane1 = LaneData.builder().build();
    LaneData outLane2 = LaneData.builder().build();
    Node<JunctionData, WayData> crossroad = new Node<>("0", JunctionData.builder().isCrossroad(false).build());
    Edge<JunctionData, WayData> inEdge = new Edge<>("1", WayData.builder()
        .lanes(List.of(inLane1, inLane2))
        .tags(Collections.singletonMap("turn:lanes", "none|merge_to_left"))
        .build());
    Edge<JunctionData, WayData> outEdge = new Edge<>("2", WayData.builder()
        .lanes(List.of(outLane1, outLane2))
        .build());

    // when
    inEdge.setSource(Mockito.mock(Node.class));
    inEdge.setTarget(crossroad);
    outEdge.setSource(crossroad);
    outEdge.setTarget(Mockito.mock(Node.class));
    crossroad.getIncomingEdges().add(inEdge);
    crossroad.getOutgoingEdges().add(outEdge);
    allocator.allocateOnNode(crossroad);

    // then
    Assertions.assertEquals(List.of(outLane1), inLane1.getAvailableSuccessors());
    Assertions.assertEquals(List.of(outLane2), inLane2.getAvailableSuccessors());
  }

  @Test
  public void bendOneLaneWithMoreLanes() {
    // given
    LaneData inLane1 = LaneData.builder().build();
    LaneData outLane1 = LaneData.builder().build();
    LaneData outLane2 = LaneData.builder().build();
    LaneData outLane3 = LaneData.builder().build();
    Node<JunctionData, WayData> crossroad = new Node<>("0", JunctionData.builder().isCrossroad(false).build());
    Edge<JunctionData, WayData> inEdge = new Edge<>("1", WayData.builder()
        .lanes(List.of(inLane1))
        .tags(Collections.emptyMap())
        .build());
    Edge<JunctionData, WayData> outEdge = new Edge<>("2", WayData.builder()
        .lanes(List.of(outLane1, outLane2, outLane3))
        .build());

    // when
    inEdge.setSource(Mockito.mock(Node.class));
    inEdge.setTarget(crossroad);
    outEdge.setSource(crossroad);
    outEdge.setTarget(Mockito.mock(Node.class));
    crossroad.getIncomingEdges().add(inEdge);
    crossroad.getOutgoingEdges().add(outEdge);
    allocator.allocateOnNode(crossroad);

    // then
    Assertions.assertTrue(inLane1.getAvailableSuccessors().contains(outLane1));
    Assertions.assertTrue(inLane1.getAvailableSuccessors().contains(outLane2));
    Assertions.assertTrue(inLane1.getAvailableSuccessors().contains(outLane3));
  }
}
