package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.type.connectivity;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.DirectBridgesConnectFixer;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.reflector.StandardEdgeReflector;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class DirectBridgesConnectFixerTest {

  private final DirectBridgesConnectFixer creator = new DirectBridgesConnectFixer(new StandardEdgeReflector());

  @Test
  public void oneSCC() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    StronglyConnectedComponent sCC1 = new StronglyConnectedComponent();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeA);
    nodeA.getIncomingEdges().add(edge2);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();
    sCC1.getNodesIds().addAll(List.of(nodeA.getId(), nodeB.getId()));

    // then
    creator.fixFoundDisconnections(List.of(sCC1), List.of(), graph);
    Assertions.assertEquals(2, graph.getNodes().size());
    Assertions.assertEquals(2, graph.getEdges().size());
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getOutgoingEdges().size() == 1));
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getIncomingEdges().size() == 1));
  }

  @Test
  public void twoSCCWithoutEdgeBetween() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    StronglyConnectedComponent sCC1 = new StronglyConnectedComponent();
    StronglyConnectedComponent sCC2 = new StronglyConnectedComponent();

    // when
    Graph<JunctionData, WayData> graph =
        new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).build();
    sCC1.getNodesIds().add(nodeA.getId());
    sCC2.getNodesIds().add(nodeB.getId());

    // then
    creator.fixFoundDisconnections(List.of(sCC1, sCC2), List.of(), graph);
    Assertions.assertEquals(2, graph.getNodes().size());
    Assertions.assertEquals(0, graph.getEdges().size());
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getOutgoingEdges().isEmpty()));
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getIncomingEdges().isEmpty()));
  }

  @Test
  public void twoSCCWithSingleEdgeBetween() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    StronglyConnectedComponent sCC1 = new StronglyConnectedComponent();
    StronglyConnectedComponent sCC2 = new StronglyConnectedComponent();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph =
        new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();
    sCC1.getNodesIds().add(nodeA.getId());
    sCC2.getNodesIds().add(nodeB.getId());
    sCC2.getExternalEdgesIds().add(edge1.getId());

    // then
    creator.fixFoundDisconnections(List.of(sCC1, sCC2), List.of(), graph);
    Assertions.assertEquals(2, graph.getNodes().size());
    Assertions.assertEquals(2, graph.getEdges().size());
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getOutgoingEdges().size() == 1));
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getIncomingEdges().size() == 1));
  }

  @Test
  public void twoExtendedSCCsWithTwoEdgesBetweenInOneDirection() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().tags(new HashMap<>()).length(1).build());
    Edge<JunctionData, WayData> edge6 = new Edge<>("6", WayData.builder().tags(new HashMap<>()).length(2).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    StronglyConnectedComponent sCC1 = new StronglyConnectedComponent();
    StronglyConnectedComponent sCC2 = new StronglyConnectedComponent();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeA);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeD);
    edge4.setSource(nodeD);
    edge4.setTarget(nodeC);
    edge5.setSource(nodeA);
    edge5.setTarget(nodeC);
    edge6.setSource(nodeB);
    edge6.setTarget(nodeD);
    nodeA.getIncomingEdges().add(edge2);
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge5));
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().addAll(List.of(edge2, edge6));
    nodeC.getIncomingEdges().addAll(List.of(edge5, edge4));
    nodeC.getOutgoingEdges().add(edge3);
    nodeD.getIncomingEdges().addAll(List.of(edge3, edge6));
    nodeD.getOutgoingEdges().add(edge4);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .addEdge(edge4)
        .addEdge(edge5)
        .addEdge(edge6)
        .build();
    sCC1.getNodesIds().addAll(List.of(nodeA.getId(), nodeB.getId()));
    sCC2.getNodesIds().addAll(List.of(nodeC.getId(), nodeD.getId()));
    sCC2.getExternalEdgesIds().addAll(List.of(edge5.getId(), edge6.getId()));

    // then
    creator.fixFoundDisconnections(List.of(sCC1, sCC2), List.of(), graph);
    Assertions.assertEquals(4, graph.getNodes().size());
    Assertions.assertEquals(7, graph.getEdges().size());
    Assertions.assertEquals(2, nodeC.getOutgoingEdges().size());
    Assertions.assertEquals(1, nodeD.getOutgoingEdges().size());
    Assertions.assertEquals(2, nodeA.getIncomingEdges().size());
    Assertions.assertEquals(1, nodeB.getIncomingEdges().size());
  }

  @Test
  public void threeSCCsWithSingleEdgeBetweenInOneDirection() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    StronglyConnectedComponent sCC1 = new StronglyConnectedComponent();
    StronglyConnectedComponent sCC2 = new StronglyConnectedComponent();
    StronglyConnectedComponent sCC3 = new StronglyConnectedComponent();

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
    nodeC.getIncomingEdges().addAll(List.of(edge3, edge2));
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    sCC1.getNodesIds().add(nodeA.getId());
    sCC2.getNodesIds().add(nodeB.getId());
    sCC3.getNodesIds().add(nodeC.getId());
    sCC2.getExternalEdgesIds().add(edge1.getId());
    sCC3.getExternalEdgesIds().addAll(List.of(edge2.getId(), edge3.getId()));

    // then
    creator.fixFoundDisconnections(List.of(sCC1, sCC2, sCC3), List.of(), graph);
    Assertions.assertEquals(3, graph.getNodes().size());
    Assertions.assertEquals(6, graph.getEdges().size());
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getOutgoingEdges().size() == 2));
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getIncomingEdges().size() == 2));
  }
}
