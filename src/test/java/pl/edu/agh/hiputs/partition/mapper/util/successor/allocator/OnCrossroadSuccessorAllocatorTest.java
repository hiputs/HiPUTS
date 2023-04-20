package pl.edu.agh.hiputs.partition.mapper.util.successor.allocator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.util.successor.pairing.DefaultPairingIncomingWithOutgoings;
import pl.edu.agh.hiputs.partition.mapper.util.turn.mapper.FixedAngleRangeTurnMapper;
import pl.edu.agh.hiputs.partition.mapper.util.turn.processor.StandardOsmTurnProcessor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.LaneData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class OnCrossroadSuccessorAllocatorTest {
  private final DefaultPairingIncomingWithOutgoings pairing = Mockito.mock(DefaultPairingIncomingWithOutgoings.class);
  private final OnCrossroadSuccessorAllocator allocator = new OnCrossroadSuccessorAllocator(
      pairing, new StandardOsmTurnProcessor(), new FixedAngleRangeTurnMapper()
  );

  @ParameterizedTest
  @MethodSource("paramsForDelegationAllocatingTest")
  public void delegationToAnotherAllocator(Map<String, String> tags) {
    // given
    LaneData inLane = LaneData.builder().build();
    LaneData outLane = LaneData.builder().build();
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).lat(50.0850637).lon(19.8911986).build());
    Node<JunctionData, WayData> startNode = new Node<>("1",
        JunctionData.builder().isCrossroad(false).lat(50.0852816).lon(19.8912199).build());
    Node<JunctionData, WayData> targetNode = new Node<>("2",
        JunctionData.builder().isCrossroad(false).lat(50.0852747).lon(19.8913083).build());
    Edge<JunctionData, WayData> inEdge = new Edge<>("3",
        WayData.builder().lanes(List.of(inLane)).tags(tags).build());
    Edge<JunctionData, WayData> outEdge = new Edge<>("4",
        WayData.builder().lanes(List.of(outLane)).build());

    // when
    inEdge.setSource(startNode);
    inEdge.setTarget(crossroad);
    outEdge.setSource(crossroad);
    outEdge.setTarget(targetNode);
    crossroad.getIncomingEdges().add(inEdge);
    crossroad.getOutgoingEdges().add(outEdge);
    allocator.allocateOnNode(crossroad);

    // then
    Mockito.verify(pairing, Mockito.times(1)).pair(inEdge.getData(), crossroad.getOutgoingEdges());
  }

  private static Stream<Arguments> paramsForDelegationAllocatingTest() {
    return Stream.of(
        Arguments.of(Collections.emptyMap()),
        Arguments.of(Collections.singletonMap("turn:lanes", "left|right")),
        Arguments.of(Collections.singletonMap("turn:lanes", "left"))
    );
  }

  @Test
  public void allocateWithOneOfThroughTurn() {
    // given
    LaneData inLane1 = LaneData.builder().build();
    LaneData inLane2 = LaneData.builder().build();
    LaneData outLane1 = LaneData.builder().build();
    LaneData outLane2 = LaneData.builder().build();
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).lat(50.0850637).lon(19.8911986).build());
    Node<JunctionData, WayData> startNode = new Node<>("1",
        JunctionData.builder().isCrossroad(false).lat(50.0843471).lon(19.8911159).build());
    Node<JunctionData, WayData> nextNode1 = new Node<>("2",
        JunctionData.builder().isCrossroad(false).lat(50.0852816).lon(19.8912199).build());
    Node<JunctionData, WayData> nextNode2 = new Node<>("3",
        JunctionData.builder().isCrossroad(false).lat(50.0852747).lon(19.8913083).build());
    Edge<JunctionData, WayData> inEdge = new Edge<>("10",
        WayData.builder()
            .lanes(List.of(inLane1, inLane2))
            .tags(Collections.singletonMap("turn:lanes", "through|through"))
            .build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("02",
        WayData.builder().lanes(List.of(outLane1)).build());
    Edge<JunctionData, WayData> outEdge2 = new Edge<>("03",
        WayData.builder().lanes(List.of(outLane2)).build());

    // when
    inEdge.setSource(startNode);
    inEdge.setTarget(crossroad);
    outEdge1.setSource(crossroad);
    outEdge1.setTarget(nextNode1);
    outEdge2.setSource(crossroad);
    outEdge2.setTarget(nextNode2);
    crossroad.getIncomingEdges().add(inEdge);
    crossroad.getOutgoingEdges().addAll(List.of(outEdge1, outEdge2));
    allocator.allocateOnNode(crossroad);

    // then
    Assertions.assertEquals(outLane2, inLane1.getAvailableSuccessors().get(0));
    Assertions.assertEquals(outLane2, inLane2.getAvailableSuccessors().get(0));
    Assertions.assertFalse(inLane1.getAvailableSuccessors().contains(outLane1));
    Assertions.assertFalse(inLane2.getAvailableSuccessors().contains(outLane1));
  }

  @Test
  public void allocateWithReverse() {
    // given
    LaneData inLane1 = LaneData.builder().build();
    LaneData inLane2 = LaneData.builder().build();
    LaneData outLane1 = LaneData.builder().build();
    LaneData outLane2 = LaneData.builder().build();
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).lat(50.0850637).lon(19.8911986).build());
    Node<JunctionData, WayData> startNode = new Node<>("1",
        JunctionData.builder().isCrossroad(false).lat(50.0843471).lon(19.8911159).build());
    Node<JunctionData, WayData> nextNode1 = new Node<>("2",
        JunctionData.builder().isCrossroad(false).lat(50.0852816).lon(19.8912199).build());
    Node<JunctionData, WayData> nextNode2 = new Node<>("3",
        JunctionData.builder().isCrossroad(false).lat(50.0852747).lon(19.8913083).build());
    Edge<JunctionData, WayData> inEdge = new Edge<>("10",
        WayData.builder()
            .lanes(List.of(inLane1, inLane2))
            .tags(Collections.singletonMap("turn:lanes", "reverse|through"))
            .build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("02",
        WayData.builder().lanes(List.of(outLane1)).build());
    Edge<JunctionData, WayData> outEdge2 = new Edge<>("03",
        WayData.builder().lanes(List.of(outLane2)).build());

    // when
    inEdge.setSource(startNode);
    inEdge.setTarget(crossroad);
    outEdge1.setSource(crossroad);
    outEdge1.setTarget(nextNode1);
    outEdge2.setSource(crossroad);
    outEdge2.setTarget(nextNode2);
    crossroad.getIncomingEdges().add(inEdge);
    crossroad.getOutgoingEdges().addAll(List.of(outEdge1, outEdge2));
    allocator.allocateOnNode(crossroad);

    // then
    Assertions.assertEquals(outLane1, inLane1.getAvailableSuccessors().get(0));
    Assertions.assertEquals(outLane2, inLane2.getAvailableSuccessors().get(0));
  }
}
