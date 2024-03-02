package pl.edu.agh.hiputs.partition.mapper.detector.util.tag.edge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.extractor.StandardEdgeExtractor;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class MaxSpeedEdgeIssuesFinderTest {

  private final MaxSpeedEdgeIssuesFinder finder = new MaxSpeedEdgeIssuesFinder(new StandardEdgeExtractor());

  @Test
  public void noEdgesFoundOnEmptyGraph() {
    // given
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().build();

    // when
    Pair<String, List<Edge<JunctionData, WayData>>> foundEdges = finder.lookup(graph);

    // then
    Assertions.assertTrue(foundEdges.getRight().isEmpty());
  }

  @Test
  public void noEdgesFoundOnRightGraphWithCrossroads() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph =
        new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();
    Pair<String, List<Edge<JunctionData, WayData>>> foundEdges = finder.lookup(graph);

    // then
    Assertions.assertTrue(foundEdges.getRight().isEmpty());
  }

  @Test
  public void noEdgesFoundOnRightGraphWithoutPredecessorAndSuccessor() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph =
        new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();
    Pair<String, List<Edge<JunctionData, WayData>>> foundEdges = finder.lookup(graph);

    // then
    Assertions.assertTrue(foundEdges.getRight().isEmpty());
  }

  @ParameterizedTest
  @MethodSource("provideParamsForMultiEdgeTest")
  public void multiEdgeTest(Map<String, String> tags1, Map<String, String> tags2, Map<String, String> tags3,
      boolean result) {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(tags1).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(tags2).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(tags3).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeD);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    nodeD.getIncomingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    Pair<String, List<Edge<JunctionData, WayData>>> foundEdges = finder.lookup(graph);

    // then
    Assertions.assertEquals(result, foundEdges.getRight().isEmpty());
  }

  private static Stream<Arguments> provideParamsForMultiEdgeTest() {
    return Stream.of(Arguments.of(new HashMap<>() {{
      put("maxspeed", "70");
    }}, new HashMap<>() {{
      put("maxspeed", "70");
    }}, new HashMap<>() {{
      put("maxspeed", "70");
    }}, true), Arguments.of(new HashMap<>() {{
      put("maxspeed", "70");
    }}, new HashMap<>() {{
      put("maxspeed", "70");
    }}, new HashMap<>() {{
      put("maxspeed", "60");
    }}, true), Arguments.of(new HashMap<>() {{
      put("maxspeed", "70");
    }}, new HashMap<>() {{
      put("maxspeed", "60");
    }}, new HashMap<>() {{
      put("maxspeed", "60");
    }}, true), Arguments.of(new HashMap<>() {{
      put("maxspeed", "70");
    }}, new HashMap<>() {{
      put("maxspeed", "60");
    }}, new HashMap<>() {{
      put("maxspeed", "50");
    }}, true), Arguments.of(new HashMap<>() {{
      put("maxspeed", "70");
    }}, new HashMap<>(), new HashMap<>() {{
      put("maxspeed", "50");
    }}, false), Arguments.of(new HashMap<>() {{
      put("maxspeed", "70");
    }}, new HashMap<>(), new HashMap<>() {{
      put("maxspeed", "70");
    }}, false), Arguments.of(new HashMap<>(), new HashMap<>(), new HashMap<>() {{
      put("maxspeed", "70");
    }}, true), Arguments.of(new HashMap<>() {{
      put("maxspeed", "70");
    }}, new HashMap<>(), new HashMap<>(), true), Arguments.of(new HashMap<>(), new HashMap<>() {{
      put("maxspeed", "70");
    }}, new HashMap<>(), true), Arguments.of(new HashMap<>(), new HashMap<>(), new HashMap<>(), true));
  }
}
