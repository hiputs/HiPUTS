package pl.edu.agh.hiputs.partition.mapper.detector.util.complex;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroad;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class ComplexRoundaboutFinderTest {

  private final ComplexRoundaboutFinder finder = new ComplexRoundaboutFinder();

  @Test
  public void emptyGraph() {
    // given

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void singleNode() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA).build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void twoSingleNodes() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());

    // when
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void twoNodesWithRoundaboutEdges() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("junction", "roundabout");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertEquals(1, complexCrossroads.size());
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeA.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeB.getId()));
  }

  @Test
  public void twoNodesWithNoRoundaboutEdges() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void threeNodesWithNoRoundaboutEdges() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeA);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void threeNodesWithOneRoundaboutEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("junction", "roundabout");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeA);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertEquals(1, complexCrossroads.size());
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeA.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeB.getId()));
  }

  @Test
  public void threeNodesWithTwoRoundaboutEdges() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("junction", "roundabout");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("junction", "roundabout");
    }}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeA);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertEquals(1, complexCrossroads.size());
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeA.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeB.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeC.getId()));
  }

  @Test
  public void threeNodesWithAllRoundaboutEdges() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("junction", "roundabout");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("junction", "roundabout");
    }}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("junction", "roundabout");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeA);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertEquals(1, complexCrossroads.size());
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeA.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeB.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeC.getId()));
  }

  @Test
  public void fourNodesWithTwoRoundabouts() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("junction", "roundabout");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("junction", "roundabout");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertEquals(2, complexCrossroads.size());
  }
}
