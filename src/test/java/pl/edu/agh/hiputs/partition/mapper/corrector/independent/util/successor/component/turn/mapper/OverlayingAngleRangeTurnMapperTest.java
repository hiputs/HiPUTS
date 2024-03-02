package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.mapper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.successor.component.turn.TurnDirection;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class OverlayingAngleRangeTurnMapperTest {
  private final OverlayingAngleRangeTurnMapper mapper = new OverlayingAngleRangeTurnMapper();

  private final Node<JunctionData, WayData> singleNode = new Node<>("-1",
      JunctionData.builder().lat(50.0843471).lon(19.8911159).build());
  private final Node<JunctionData, WayData> centerNode = new Node<>("0",
      JunctionData.builder().lat(50.0850637).lon(19.8911986).build());
  private final Node<JunctionData, WayData> leftSiblingNode = new Node<>("1",
      JunctionData.builder().lat(50.0852816).lon(19.8912199).build());
  private final Node<JunctionData, WayData> rightSiblingNode = new Node<>("2",
      JunctionData.builder().lat(50.0852747).lon(19.8913083).build());

  private final Edge<JunctionData, WayData> refEdge = new Edge<>("ref", WayData.builder().build());
  private final Edge<JunctionData, WayData> nextEdge1 = new Edge<>("n1", WayData.builder().build());
  private final Edge<JunctionData, WayData> nextEdge2 = new Edge<>("n2", WayData.builder().build());

  @Test
  public void oneOutgoingInEveryAngleRangeBeforeFlatMap() {
    // given

    // when
    refEdge.setSource(leftSiblingNode);
    refEdge.setTarget(centerNode);
    nextEdge1.setSource(centerNode);
    nextEdge1.setTarget(rightSiblingNode);
    nextEdge2.setSource(centerNode);
    nextEdge2.setTarget(singleNode);
    Map<TurnDirection, Edge<JunctionData, WayData>> mapOfTurnAndEdges = mapper.assignTurns2OutgoingEdges(
        List.of(nextEdge1, nextEdge2), refEdge
    );

    // then
    Assertions.assertEquals(3, mapOfTurnAndEdges.size());
    Assertions.assertEquals(2, mapOfTurnAndEdges.entrySet().stream()
        .filter(entry -> entry.getValue().equals(nextEdge2))
        .count());
    Assertions.assertEquals(1, mapOfTurnAndEdges.entrySet().stream()
        .filter(entry -> entry.getValue().equals(nextEdge1))
        .count());
    Assertions.assertTrue(mapOfTurnAndEdges.containsKey(TurnDirection.SHARP_LEFT));
    Assertions.assertTrue(mapOfTurnAndEdges.containsKey(TurnDirection.THROUGH));
    Assertions.assertTrue(mapOfTurnAndEdges.containsKey(TurnDirection.SLIGHT_RIGHT));
  }

  @Test
  public void moreOutgoingsInEveryAngleRangeBeforeFlatMap() {
    // given

    // when
    refEdge.setSource(singleNode);
    refEdge.setTarget(centerNode);
    nextEdge1.setSource(centerNode);
    nextEdge1.setTarget(leftSiblingNode);
    nextEdge2.setSource(centerNode);
    nextEdge2.setTarget(rightSiblingNode);
    Map<TurnDirection, Edge<JunctionData, WayData>> mapOfTurnAndEdges = mapper.assignTurns2OutgoingEdges(
        List.of(nextEdge1, nextEdge2), refEdge
    );

    // then
    Assertions.assertEquals(3, mapOfTurnAndEdges.size());
    Assertions.assertEquals(2, mapOfTurnAndEdges.entrySet().stream()
        .filter(entry -> entry.getValue().equals(nextEdge2))
        .count());
    Assertions.assertEquals(1, mapOfTurnAndEdges.entrySet().stream()
        .filter(entry -> entry.getValue().equals(nextEdge1))
        .count());
    Assertions.assertTrue(mapOfTurnAndEdges.containsKey(TurnDirection.SLIGHT_LEFT));
    Assertions.assertTrue(mapOfTurnAndEdges.containsKey(TurnDirection.THROUGH));
    Assertions.assertTrue(mapOfTurnAndEdges.containsKey(TurnDirection.SLIGHT_RIGHT));
    mapOfTurnAndEdges.entrySet().stream()
        .filter(entry -> entry.getValue().equals(nextEdge1))
        .map(Entry::getKey)
        .findAny()
        .ifPresent(turn -> Assertions.assertEquals(TurnDirection.SLIGHT_LEFT, turn));
  }
}
