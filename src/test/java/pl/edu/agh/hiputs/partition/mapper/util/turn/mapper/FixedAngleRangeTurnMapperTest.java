package pl.edu.agh.hiputs.partition.mapper.util.turn.mapper;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.util.turn.TurnDirection;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class FixedAngleRangeTurnMapperTest {
  private final FixedAngleRangeTurnMapper mapper = new FixedAngleRangeTurnMapper();

  @Test
  public void oneOutgoingPerOneAngleRange() {
    // given
    JunctionData centerData = JunctionData.builder().lat(50.0850637).lon(19.8911986).build();
    JunctionData startData = JunctionData.builder().lat(50.0852816).lon(19.8912199).build();
    JunctionData nextData = JunctionData.builder().lat(50.0852747).lon(19.8913083).build();
    Node<JunctionData, WayData> centerNode = new Node<>("1", centerData);
    Node<JunctionData, WayData> startNode = new Node<>("2", startData);
    Node<JunctionData, WayData> nextNode = new Node<>("3", nextData);
    Edge<JunctionData, WayData> refEdge = new Edge<>("12", WayData.builder().build());
    Edge<JunctionData, WayData> nextEdge = new Edge<>("13", WayData.builder().build());

    // when
    refEdge.setSource(startNode);
    refEdge.setTarget(centerNode);
    nextEdge.setSource(centerNode);
    nextEdge.setTarget(nextNode);

    // then
    Map<TurnDirection, Edge<JunctionData, WayData>> mapOfTurnAndEdges = mapper.assignTurns2OutgoingEdges(
        List.of(nextEdge), refEdge
    );
    Assertions.assertEquals(1, mapOfTurnAndEdges.size());
    Assertions.assertTrue(mapOfTurnAndEdges.containsKey(TurnDirection.SHARP_LEFT));
  }

  @Test
  public void moreOutgoingPerOneAngleRange() {
    // given
    JunctionData startData = JunctionData.builder().lat(50.0843471).lon(19.8911159).build();
    JunctionData centerData = JunctionData.builder().lat(50.0850637).lon(19.8911986).build();
    JunctionData nextData1 = JunctionData.builder().lat(50.0852816).lon(19.8912199).build();
    JunctionData nextData2 = JunctionData.builder().lat(50.0852747).lon(19.8913083).build();
    Node<JunctionData, WayData> startNode = new Node<>("3", startData);
    Node<JunctionData, WayData> centerNode = new Node<>("0", centerData);
    Node<JunctionData, WayData> nextNode1 = new Node<>("1", nextData1);
    Node<JunctionData, WayData> nextNode2 = new Node<>("2", nextData2);
    Edge<JunctionData, WayData> refEdge = new Edge<>("30", WayData.builder().build());
    Edge<JunctionData, WayData> nextEdge1 = new Edge<>("01", WayData.builder().build());
    Edge<JunctionData, WayData> nextEdge2 = new Edge<>("02", WayData.builder().build());


    // when
    refEdge.setSource(startNode);
    refEdge.setTarget(centerNode);
    nextEdge1.setSource(centerNode);
    nextEdge1.setTarget(nextNode1);
    nextEdge2.setSource(centerNode);
    nextEdge2.setTarget(nextNode2);

    // then
    Map<TurnDirection, Edge<JunctionData, WayData>> mapOfTurnAndEdges = mapper.assignTurns2OutgoingEdges(
        List.of(nextEdge1, nextEdge2), refEdge
    );
    Assertions.assertEquals(1, mapOfTurnAndEdges.size());
    Assertions.assertTrue(mapOfTurnAndEdges.containsKey(TurnDirection.THROUGH));
    Assertions.assertEquals(nextEdge2, mapOfTurnAndEdges.get(TurnDirection.THROUGH));
  }
}
