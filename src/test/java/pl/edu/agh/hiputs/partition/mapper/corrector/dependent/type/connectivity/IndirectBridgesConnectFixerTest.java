package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.type.connectivity;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.IndirectBridgesConnectFixer;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class IndirectBridgesConnectFixerTest {
  private final IndirectBridgesConnectFixer creator = new IndirectBridgesConnectFixer();

  @Test
  public void oneWCC() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeA);
    nodeA.getIncomingEdges().add(edge2);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();
    wCC1.getNodesIds().addAll(List.of(nodeA.getId(), nodeB.getId()));

    // then
    creator.fixFoundDisconnections(List.of(), List.of(wCC1), graph);
    Assertions.assertEquals(2, graph.getNodes().size());
    Assertions.assertEquals(2, graph.getEdges().size());
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getOutgoingEdges().size() == 1));
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getIncomingEdges().size() == 1));
  }

  @Test
  public void twoWCCsAndOneIsWithoutNodes() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();
    WeaklyConnectedComponent wCC2 = new WeaklyConnectedComponent();

    // when
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .build();
    wCC1.getNodesIds().add(nodeA.getId());

    // then
    creator.fixFoundDisconnections(List.of(), List.of(wCC1, wCC2), graph);
    Assertions.assertEquals(2, graph.getNodes().size());
    Assertions.assertEquals(0, graph.getEdges().size());
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getOutgoingEdges().isEmpty()));
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getIncomingEdges().isEmpty()));
  }

  @Test
  public void twoWCCsWithOneNodeInBoth() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .lon(50.1257849).lat(19.9104581).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .lon(50.1274453).lat(19.9121929).build());
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();
    WeaklyConnectedComponent wCC2 = new WeaklyConnectedComponent();

    // when
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .build();
    wCC1.getNodesIds().add(nodeA.getId());
    wCC2.getNodesIds().add(nodeB.getId());

    // then
    creator.fixFoundDisconnections(List.of(), List.of(wCC1, wCC2), graph);
    Assertions.assertEquals(2, graph.getNodes().size());
    Assertions.assertEquals(2, graph.getEdges().size());
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getOutgoingEdges().size() == 1));
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getIncomingEdges().size() == 1));
  }

  @Test
  public void twoWCCsWithOneNodeInBothAndEdgesFurther() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary"); put("name", "Komputerowa");}})
        .build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder()
        .tags(new HashMap<>(){{put("maxspeed", "50"); put("name", "Laptopowa");}})
        .build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder()
        .tags(new HashMap<>(){{put("addr:country", "PL"); put("name", "Komputerowa");}})
        .build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .lon(50.1257849).lat(19.9104581).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .lon(50.1274453).lat(19.9121929).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeE = new Node<>("E", JunctionData.builder().build());
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();
    WeaklyConnectedComponent wCC2 = new WeaklyConnectedComponent();

    // when
    edge1.setSource(nodeC);
    edge1.setTarget(nodeA);
    edge2.setSource(nodeD);
    edge2.setTarget(nodeA);
    edge3.setSource(nodeB);
    edge3.setTarget(nodeE);
    nodeA.getIncomingEdges().addAll(List.of(edge1, edge2));
    nodeB.getOutgoingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addNode(nodeE)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    wCC1.getNodesIds().add(nodeA.getId());
    wCC2.getNodesIds().add(nodeB.getId());

    // then
    creator.fixFoundDisconnections(List.of(), List.of(wCC1, wCC2), graph);
    Assertions.assertEquals(5, graph.getNodes().size());
    Assertions.assertEquals(5, graph.getEdges().size());
    Assertions.assertEquals(1, nodeA.getOutgoingEdges().size());
    Assertions.assertEquals(3, nodeA.getIncomingEdges().size());
    Assertions.assertEquals(2, nodeB.getOutgoingEdges().size());
    Assertions.assertEquals(1, nodeB.getIncomingEdges().size());
    Assertions.assertEquals("primary", nodeA.getOutgoingEdges().get(0).getData().getTags().get("highway"));
    Assertions.assertEquals("Komputerowa", nodeA.getOutgoingEdges().get(0).getData().getTags().get("name"));
    Assertions.assertEquals("50", nodeA.getOutgoingEdges().get(0).getData().getTags().get("maxspeed"));
    Assertions.assertEquals("PL", nodeA.getOutgoingEdges().get(0).getData().getTags().get("addr:country"));
  }

  @Test
  public void twoWCCsWithMoreNodesInBoth() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .lon(50.1254876).lat(19.9106595).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .lon(50.1257849).lat(19.9104581).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder()
        .lon(50.1275802).lat(19.9123953).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder()
        .lon(50.1274453).lat(19.9121929).build());
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();
    WeaklyConnectedComponent wCC2 = new WeaklyConnectedComponent();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeD);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    nodeD.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();
    wCC1.getNodesIds().addAll(List.of(nodeA.getId(), nodeB.getId()));
    wCC2.getNodesIds().addAll(List.of(nodeC.getId(), nodeD.getId()));

    // then
    creator.fixFoundDisconnections(List.of(), List.of(wCC1, wCC2), graph);
    Assertions.assertEquals(4, graph.getNodes().size());
    Assertions.assertEquals(4, graph.getEdges().size());
    Assertions.assertEquals(2, nodeB.getIncomingEdges().size());
    Assertions.assertEquals(1, nodeB.getOutgoingEdges().size());
    Assertions.assertEquals(2, nodeD.getIncomingEdges().size());
    Assertions.assertEquals(1, nodeD.getOutgoingEdges().size());
  }

  @Test
  public void threeWCCsWithNodesOnly() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .lon(50.1254876).lat(19.9106595).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .lon(50.1257849).lat(19.9104581).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder()
        .lon(50.1275802).lat(19.9123953).build());
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();
    WeaklyConnectedComponent wCC2 = new WeaklyConnectedComponent();
    WeaklyConnectedComponent wCC3 = new WeaklyConnectedComponent();

    // when
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .build();
    wCC1.getNodesIds().add(nodeA.getId());
    wCC2.getNodesIds().add(nodeB.getId());
    wCC3.getNodesIds().add(nodeC.getId());

    // then
    creator.fixFoundDisconnections(List.of(), List.of(wCC1, wCC2, wCC3), graph);
    Assertions.assertEquals(3, graph.getNodes().size());
    Assertions.assertEquals(6, graph.getEdges().size());
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getOutgoingEdges().size() == 2));
    Assertions.assertTrue(graph.getNodes().values().stream().allMatch(node -> node.getIncomingEdges().size() == 2));
  }
}
