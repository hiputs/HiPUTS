package pl.edu.agh.hiputs.partition.mapper.transformer;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class GraphReverseRoadsCreatorTest {
  private final GraphReverseRoadsCreator creator = new GraphReverseRoadsCreator();
  private final Map<String, String> tags = Map.of("lanes", "1");
  private final Node<JunctionData, WayData> center = new Node<>("0", JunctionData.builder().build());
  private final Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder().build());
  private final Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder().build());
  private final WayData wayData = WayData.builder()
      .tags(tags)
      .tagsInOppositeMeaning(true)
      .length(10.1)
      .maxSpeed(30)
      .isPriorityRoad(false)
      .patchId("123")
      .build();

  @BeforeEach
  public void cleanUpBeforeTest() {
    center.getIncomingEdges().clear();
    center.getOutgoingEdges().clear();
  }

  @Test
  public void createOnDeadEnds() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("10", wayData);
    Edge<JunctionData, WayData> edge2 = new Edge<>("02", WayData.builder().build());
    Edge<JunctionData, WayData> edgeBack = new Edge<>("120", WayData.builder().build());
    edge1.setSource(node1);
    edge1.setTarget(center);
    edge2.setSource(center);
    edge2.setTarget(node2);
    edgeBack.setSource(node2);
    edgeBack.setTarget(center);
    center.getIncomingEdges().addAll(List.of(edge1, edgeBack));
    center.getOutgoingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(center)
        .addNode(node1)
        .addNode(node2)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edgeBack)
        .build();

    // when
    creator.transform(graph);

    // then
    Assertions.assertEquals(2, center.getOutgoingEdges().size());
    Assertions.assertEquals(1, center.getOutgoingEdges().stream()
        .filter(edge -> edge.getTarget().equals(edge1.getSource()))
        .toList().size());
    center.getOutgoingEdges().stream()
        .filter(edge -> edge.getTarget().equals(edge1.getSource()))
        .findAny().ifPresent(returned -> {
          Assertions.assertEquals(tags, returned.getData().getTags());
          Assertions.assertFalse(returned.getData().isTagsInOppositeMeaning());
          Assertions.assertFalse(returned.getData().isPriorityRoad());
          Assertions.assertEquals(10.1, returned.getData().getLength());
          Assertions.assertEquals(30, returned.getData().getMaxSpeed());
          Assertions.assertEquals("123", returned.getData().getPatchId());
        });
  }

  @Test
  public void createOnNotReachableBeginnings() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("10", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("02", wayData);
    Edge<JunctionData, WayData> edgeBack = new Edge<>("120", WayData.builder().build());
    edge1.setSource(node1);
    edge1.setTarget(center);
    edge2.setSource(center);
    edge2.setTarget(node2);
    edgeBack.setSource(center);
    edgeBack.setTarget(node1);
    center.getIncomingEdges().add(edge1);
    center.getOutgoingEdges().addAll(List.of(edge2, edgeBack));
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(center)
        .addNode(node1)
        .addNode(node2)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edgeBack)
        .build();

    // when
    creator.transform(graph);

    // then
    Assertions.assertEquals(2, center.getIncomingEdges().size());
    Assertions.assertEquals(1, center.getIncomingEdges().stream()
        .filter(edge -> edge.getSource().equals(edge2.getTarget()))
        .toList().size());
    center.getIncomingEdges().stream()
        .filter(edge -> edge.getSource().equals(edge2.getTarget()))
        .findAny().ifPresent(returned -> {
          Assertions.assertEquals(tags, returned.getData().getTags());
          Assertions.assertFalse(returned.getData().isTagsInOppositeMeaning());
          Assertions.assertFalse(returned.getData().isPriorityRoad());
          Assertions.assertEquals(10.1, returned.getData().getLength());
          Assertions.assertEquals(30, returned.getData().getMaxSpeed());
          Assertions.assertEquals("123", returned.getData().getPatchId());
        });
  }
}
