package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.type.connectivity;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.ByWCCLargestConnectFixer;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class ByWCCLargestConnectFixerTest {

  private final ByWCCLargestConnectFixer fixer = new ByWCCLargestConnectFixer();

  @Test
  public void emptyGraph() {
    // given

    // when
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().build();

    // then
    Graph<JunctionData, WayData> result = fixer.fixFoundDisconnections(List.of(), List.of(), graph);
    Assertions.assertEquals(result, graph);
  }

  @Test
  public void singleNodeGraph() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();

    // when
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).build();
    wCC1.addNode(nodeA.getId());

    // then
    Graph<JunctionData, WayData> result = fixer.fixFoundDisconnections(List.of(), List.of(wCC1), graph);
    Assertions.assertEquals(1, result.getNodes().size());
  }

  @Test
  public void twoNodesWithEdgeGraph() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph =
        new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();
    wCC1.addNode(nodeA.getId());
    wCC1.addNode(nodeB.getId());

    // then
    Graph<JunctionData, WayData> result = fixer.fixFoundDisconnections(List.of(), List.of(wCC1), graph);
    Assertions.assertEquals(2, result.getNodes().size());
    Assertions.assertEquals(1, result.getEdges().size());
  }

  @Test
  public void threeNodesWithEdgeGraph() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();
    WeaklyConnectedComponent wCC2 = new WeaklyConnectedComponent();

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
    wCC1.addNode(nodeA.getId());
    wCC1.addNode(nodeB.getId());
    wCC2.addNode(nodeC.getId());

    // then
    Graph<JunctionData, WayData> result = fixer.fixFoundDisconnections(List.of(), List.of(wCC1), graph);
    Assertions.assertEquals(2, result.getNodes().size());
    Assertions.assertEquals(1, result.getEdges().size());
  }

  @Test
  public void sixNodesWith3WCCsGraph() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeE = new Node<>("E", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeF = new Node<>("F", JunctionData.builder().build());
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();
    WeaklyConnectedComponent wCC2 = new WeaklyConnectedComponent();
    WeaklyConnectedComponent wCC3 = new WeaklyConnectedComponent();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeA);
    edge4.setSource(nodeD);
    edge4.setTarget(nodeE);
    nodeA.getIncomingEdges().add(edge3);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    nodeE.getIncomingEdges().add(edge4);
    nodeD.getOutgoingEdges().add(edge4);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addNode(nodeE)
        .addNode(nodeF)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .addEdge(edge4)
        .build();
    wCC1.addNode(nodeA.getId());
    wCC1.addNode(nodeB.getId());
    wCC1.addNode(nodeC.getId());
    wCC2.addNode(nodeD.getId());
    wCC2.addNode(nodeE.getId());
    wCC3.addNode(nodeF.getId());

    // then
    Graph<JunctionData, WayData> result = fixer.fixFoundDisconnections(List.of(), List.of(wCC1, wCC2, wCC3), graph);
    Assertions.assertEquals(3, result.getNodes().size());
    Assertions.assertEquals(3, result.getEdges().size());
  }

  @Test
  public void fourNodesWith2WCCsConnectedGraph() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();
    WeaklyConnectedComponent wCC2 = new WeaklyConnectedComponent();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeA);
    edge4.setSource(nodeD);
    edge4.setTarget(nodeA);
    nodeA.getIncomingEdges().addAll(List.of(edge3, edge4));
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    nodeD.getOutgoingEdges().add(edge4);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .addEdge(edge4)
        .build();
    wCC1.addNode(nodeA.getId());
    wCC1.addNode(nodeB.getId());
    wCC1.addNode(nodeC.getId());
    wCC2.addNode(nodeD.getId());

    // then
    Graph<JunctionData, WayData> result = fixer.fixFoundDisconnections(List.of(), List.of(wCC1, wCC2), graph);
    Assertions.assertEquals(3, result.getNodes().size());
    Assertions.assertEquals(3, result.getEdges().size());
  }
}
