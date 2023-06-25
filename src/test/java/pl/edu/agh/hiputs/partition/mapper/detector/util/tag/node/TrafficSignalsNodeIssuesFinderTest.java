package pl.edu.agh.hiputs.partition.mapper.detector.util.tag.node;

import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class TrafficSignalsNodeIssuesFinderTest {
  private final TrafficSignalsNodeIssuesFinder finder = new TrafficSignalsNodeIssuesFinder();

  @Test
  public void noNodesFoundInEmptyGraph() {
    // given
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().build();

    // when
    Pair<String, List<Node<JunctionData, WayData>>> foundNodes = finder.lookup(graph);

    // then
    Assertions.assertTrue(foundNodes.getRight().isEmpty());
  }

  @Test
  public void noNodesFoundInGraphWithSingleNodeWithoutTag() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().tags(new HashMap<>()).build());
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .build();

    // when
    Pair<String, List<Node<JunctionData, WayData>>> foundNodes = finder.lookup(graph);

    // then
    Assertions.assertTrue(foundNodes.getRight().isEmpty());
  }

  @Test
  public void nodeFoundInGraphWithSingleTrafficSignalNode() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .tags(new HashMap<>(){{put("highway", "traffic_signals");}}).build());
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .build();

    // when
    Pair<String, List<Node<JunctionData, WayData>>> foundNodes = finder.lookup(graph);

    // then
    Assertions.assertFalse(foundNodes.getRight().isEmpty());
  }

  @Test
  public void noNodesFoundInGraphWithSingleTrafficSignalNodeAndEdges() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .tags(new HashMap<>(){{put("highway", "traffic_signals");}}).build());
    nodeA.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .build();

    // when
    Pair<String, List<Node<JunctionData, WayData>>> foundNodes = finder.lookup(graph);

    // then
    Assertions.assertTrue(foundNodes.getRight().isEmpty());
  }
}
