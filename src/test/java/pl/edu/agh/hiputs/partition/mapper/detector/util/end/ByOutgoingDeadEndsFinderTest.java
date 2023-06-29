package pl.edu.agh.hiputs.partition.mapper.detector.util.end;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class ByOutgoingDeadEndsFinderTest {
  private final ByOutgoingDeadEndsFinder finder = new ByOutgoingDeadEndsFinder();

  @Test
  public void emptyGraph() {
    // given

    // when
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().build();

    // then
    List<DeadEnd> foundDeadEnds = finder.lookup(graph);
    Assertions.assertEquals(0, foundDeadEnds.size());
  }

  @Test
  public void singleNodes() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());

    // when
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .build();

    // then
    List<DeadEnd> foundDeadEnds = finder.lookup(graph);
    Assertions.assertEquals(0, foundDeadEnds.size());
  }

  @Test
  public void singleEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addEdge(edge1)
        .build();

    // then
    List<DeadEnd> foundDeadEnds = finder.lookup(graph);
    Assertions.assertEquals(1, foundDeadEnds.size());
    Assertions.assertEquals(nodeA, foundDeadEnds.get(0).getNodeStarting());
    Assertions.assertEquals(1, foundDeadEnds.get(0).getConnectingEdges().size());
  }

  @Test
  public void twoEdgesOneByOne() {
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
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<DeadEnd> foundDeadEnds = finder.lookup(graph);
    Assertions.assertEquals(1, foundDeadEnds.size());
    Assertions.assertEquals(nodeA, foundDeadEnds.get(0).getNodeStarting());
    Assertions.assertEquals(2, foundDeadEnds.get(0).getConnectingEdges().size());
  }

  @Test
  public void twoEdgesWithOneCommonNodeAndReversedDirection() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeB);
    edge1.setTarget(nodeA);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeA);
    nodeA.getIncomingEdges().addAll(List.of(edge1, edge2));
    nodeB.getOutgoingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<DeadEnd> foundDeadEnds = finder.lookup(graph);
    Assertions.assertEquals(2, foundDeadEnds.size());
    Assertions.assertEquals(nodeB, foundDeadEnds.get(0).getNodeStarting());
    Assertions.assertEquals(nodeC, foundDeadEnds.get(1).getNodeStarting());
    Assertions.assertEquals(edge1, foundDeadEnds.get(0).getConnectingEdges().get(0));
    Assertions.assertEquals(edge2, foundDeadEnds.get(1).getConnectingEdges().get(0));
  }

  @Test
  public void twoEdgesWithOneCommonNode() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeA);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge2));
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<DeadEnd> foundDeadEnds = finder.lookup(graph);
    Assertions.assertEquals(0, foundDeadEnds.size());
  }

  @Test
  public void threeEdgesInTreeShape() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeA);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeD);
    edge3.setTarget(nodeA);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge2));
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getIncomingEdges().add(edge2);
    nodeD.getOutgoingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<DeadEnd> foundDeadEnds = finder.lookup(graph);
    Assertions.assertEquals(1, foundDeadEnds.size());
    Assertions.assertEquals(nodeD, foundDeadEnds.get(0).getNodeStarting());
    Assertions.assertEquals(edge3, foundDeadEnds.get(0).getConnectingEdges().get(0));
  }

  @Test
  public void threeEdgesInReversedTreeShape() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeA);
    edge3.setSource(nodeD);
    edge3.setTarget(nodeA);
    nodeA.getOutgoingEdges().add(edge1);
    nodeA.getIncomingEdges().addAll(List.of(edge2, edge3));
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    nodeD.getOutgoingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<DeadEnd> foundDeadEnds = finder.lookup(graph);
    Assertions.assertEquals(2, foundDeadEnds.size());
    Assertions.assertEquals(nodeC, foundDeadEnds.get(0).getNodeStarting());
    Assertions.assertEquals(nodeD, foundDeadEnds.get(1).getNodeStarting());
    Assertions.assertEquals(edge2, foundDeadEnds.get(0).getConnectingEdges().get(0));
    Assertions.assertEquals(edge3, foundDeadEnds.get(1).getConnectingEdges().get(0));
  }

  @Test
  public void complexGraphWithMultipleDeadEnds() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeE = new Node<>("E", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeD);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeA);
    edge4.setSource(nodeA);
    edge4.setTarget(nodeE);
    edge5.setSource(nodeE);
    edge5.setTarget(nodeA);
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge4));
    nodeA.getIncomingEdges().addAll(List.of(edge5, edge3));
    nodeB.getIncomingEdges().add(edge1);
    nodeD.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    nodeE.getIncomingEdges().add(edge4);
    nodeE.getOutgoingEdges().add(edge5);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addNode(nodeE)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .addEdge(edge4)
        .addEdge(edge5)
        .build();

    // then
    List<DeadEnd> foundDeadEnds = finder.lookup(graph);
    Assertions.assertEquals(1, foundDeadEnds.size());
    Assertions.assertEquals(nodeD, foundDeadEnds.get(0).getNodeStarting());
    Assertions.assertEquals(2, foundDeadEnds.get(0).getConnectingEdges().size());
  }
}
