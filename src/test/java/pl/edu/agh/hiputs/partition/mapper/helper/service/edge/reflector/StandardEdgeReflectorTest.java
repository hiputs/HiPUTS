package pl.edu.agh.hiputs.partition.mapper.helper.service.edge.reflector;

import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class StandardEdgeReflectorTest {
  private final StandardEdgeReflector reflector = new StandardEdgeReflector();

  @Test
  public void reverseEmptyEdge() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge = new Edge<>(nodeA.getId() + "->" + nodeB.getId(), WayData.builder().build());
    edge.setSource(nodeA);
    edge.setTarget(nodeB);

    // when
    Edge<JunctionData, WayData> reversedEdge = reflector.reverseEdge(edge);

    // then
    Assertions.assertEquals(nodeB, reversedEdge.getSource());
    Assertions.assertEquals(nodeA, reversedEdge.getTarget());
    Assertions.assertTrue(reversedEdge.getData().isOneWay());
    Assertions.assertEquals(edge.getData().getTags(), reversedEdge.getData().getTags());
    Assertions.assertTrue(reversedEdge.getData().isTagsInOppositeMeaning());
    Assertions.assertTrue(reversedEdge.getData().getLanes().isEmpty());
    Assertions.assertEquals(0, reversedEdge.getData().getLength());
    Assertions.assertEquals(0, reversedEdge.getData().getMaxSpeed());
    Assertions.assertFalse(reversedEdge.getData().isPriorityRoad());
    Assertions.assertNull(reversedEdge.getData().getPatchId());
    Assertions.assertTrue(reversedEdge.getData().getTrafficIndicator().isEmpty());
  }

  @Test
  public void reverseFilledEdge() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Edge<JunctionData, WayData> edge = new Edge<>(nodeA.getId() + "->" + nodeB.getId(), WayData.builder()
        .isOneWay(true)
        .tagsInOppositeMeaning(true)
        .tags(new HashMap<>(){{put("highway", "primary");}})
        .length(2.0)
        .maxSpeed(50)
        .isPriorityRoad(false)
        .patchId("PATCH1")
        .build());
    edge.setSource(nodeA);
    edge.setTarget(nodeB);

    // when
    Edge<JunctionData, WayData> reversedEdge = reflector.reverseEdge(edge);

    // then
    Assertions.assertEquals(nodeB, reversedEdge.getSource());
    Assertions.assertEquals(nodeA, reversedEdge.getTarget());
    Assertions.assertTrue(reversedEdge.getData().isOneWay());
    Assertions.assertEquals(edge.getData().getTags(), reversedEdge.getData().getTags());
    Assertions.assertEquals(edge.getData().isTagsInOppositeMeaning(), !reversedEdge.getData().isTagsInOppositeMeaning());
    Assertions.assertEquals(edge.getData().getLength(), reversedEdge.getData().getLength());
    Assertions.assertEquals(edge.getData().getMaxSpeed(), reversedEdge.getData().getMaxSpeed());
    Assertions.assertEquals(edge.getData().isPriorityRoad(), reversedEdge.getData().isPriorityRoad());
    Assertions.assertEquals(edge.getData().getPatchId(), reversedEdge.getData().getPatchId());
  }
}
