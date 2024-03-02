package pl.edu.agh.hiputs.partition.mapper.detector.util.connectivity;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class KosarajuSCCFinderTest {

  private final KosarajuSCCFinder finder = new KosarajuSCCFinder();

  @Test
  public void oneNodeOneSCC() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());

    // when
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).build();

    // then
    List<StronglyConnectedComponent> foundSCCs = finder.lookup(graph);
    Assertions.assertEquals(1, foundSCCs.size());
  }

  @Test
  public void twoNodesTwoSCCs() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());

    // when
    Graph<JunctionData, WayData> graph =
        new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).build();

    // then
    List<StronglyConnectedComponent> foundSCCs = finder.lookup(graph);
    Assertions.assertEquals(2, foundSCCs.size());
  }

  @Test
  public void twoNodesWithEdgeBetweenTwoSCCs() {
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
        new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();

    // then
    List<StronglyConnectedComponent> foundSCCs = finder.lookup(graph);
    Assertions.assertEquals(2, foundSCCs.size());
  }

  @Test
  public void twoNodesWithReversedEdgeBetweenTwoSCCs() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());

    // when
    edge1.setSource(nodeB);
    edge1.setTarget(nodeA);
    nodeB.getOutgoingEdges().add(edge1);
    nodeA.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph =
        new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();

    // then
    List<StronglyConnectedComponent> foundSCCs = finder.lookup(graph);
    Assertions.assertEquals(2, foundSCCs.size());
  }

  @Test
  public void threeNodesWithEdgeBetweenTwoAndThreeSCC() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .build();

    // then
    List<StronglyConnectedComponent> foundSCCs = finder.lookup(graph);
    Assertions.assertEquals(3, foundSCCs.size());
  }

  @Test
  public void threeNodesWithTwoEdgesBetweenAllAndThreeSCC() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<StronglyConnectedComponent> foundSCCs = finder.lookup(graph);
    Assertions.assertEquals(3, foundSCCs.size());
  }

  @Test
  public void threeNodesWithLoopAndOneSCC() {
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
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<StronglyConnectedComponent> foundSCCs = finder.lookup(graph);
    Assertions.assertEquals(1, foundSCCs.size());
  }

  @Test
  public void threeNodesWithThreeSCCs() {
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
    edge3.setSource(nodeA);
    edge3.setTarget(nodeC);
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge3));
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().addAll(List.of(edge2, edge3));
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<StronglyConnectedComponent> foundSCCs = finder.lookup(graph);
    Assertions.assertEquals(3, foundSCCs.size());
  }

  @Test
  public void threeNodesWithReversedEdgeAndTwoSCCs() {
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
    edge3.setSource(nodeB);
    edge3.setTarget(nodeA);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().addAll(List.of(edge2, edge3));
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<StronglyConnectedComponent> foundSCCs = finder.lookup(graph);
    Assertions.assertEquals(2, foundSCCs.size());
  }

  @Test
  public void threeNodesWithReversedEdgesButOnePairWithoutEdgesAndOneSCCs() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeB);
    edge3.setTarget(nodeA);
    edge4.setSource(nodeC);
    edge4.setTarget(nodeB);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().addAll(List.of(edge1, edge4));
    nodeB.getOutgoingEdges().addAll(List.of(edge2, edge3));
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge4);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .addEdge(edge4)
        .build();

    // then
    List<StronglyConnectedComponent> foundSCCs = finder.lookup(graph);
    Assertions.assertEquals(1, foundSCCs.size());
  }
}
