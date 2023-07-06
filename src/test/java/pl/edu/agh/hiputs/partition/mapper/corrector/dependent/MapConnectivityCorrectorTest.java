package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.AllBridgesConnectFixer;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.DirectBridgesConnectFixer;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.IndirectBridgesConnectFixer;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.reflector.StandardEdgeReflector;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class MapConnectivityCorrectorTest {

  @Test
  public void cleanUpSCCS() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("A->B", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("B->A", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("C->A", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("A->D", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().build());
    StronglyConnectedComponent sCC1 = new StronglyConnectedComponent();
    StronglyConnectedComponent sCC2 = new StronglyConnectedComponent();
    StronglyConnectedComponent sCC3 = new StronglyConnectedComponent();
    AllBridgesConnectFixer creator = Mockito.mock(AllBridgesConnectFixer.class);

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeA);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeA);
    edge4.setSource(nodeA);
    edge4.setTarget(nodeD);
    nodeA.getIncomingEdges().addAll(List.of(edge2, edge3));
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge4));
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    nodeD.getIncomingEdges().add(edge4);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .addEdge(edge4)
        .build();
    sCC1.getNodesIds().addAll(List.of(nodeA.getId(), nodeB.getId()));
    sCC1.getExternalEdgesIds().addAll(List.of(edge1.getId(), edge2.getId(), edge3.getId()));
    sCC2.getNodesIds().add(nodeC.getId());
    sCC3.getNodesIds().add(nodeD.getId());
    sCC3.getExternalEdgesIds().add(edge4.getId());
    MapConnectivityCorrector corrector = new MapConnectivityCorrector(List.of(sCC1, sCC2, sCC3), List.of(), creator);
    Mockito.when(creator.fixFoundDisconnections(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(graph);

    //then
    corrector.correct(graph);
    Assertions.assertEquals(1, sCC1.getExternalEdgesIds().size());
    Assertions.assertEquals(0, sCC2.getExternalEdgesIds().size());
    Assertions.assertEquals(1, sCC3.getExternalEdgesIds().size());
  }

  @Test
  public void happyPath() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("A->B", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("C->D", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("D->C", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("C->E", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("E->C", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .lon(50.1254876).lat(19.9106595).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .lon(50.1257849).lat(19.9104581).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder()
        .lon(50.1275802).lat(19.9123953).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder()
        .lon(50.1274453).lat(19.9121929).build());
    Node<JunctionData, WayData> nodeE = new Node<>("E", JunctionData.builder()
        .lon(50.1276259).lat(19.9128388).build());
    StronglyConnectedComponent sCC1 = new StronglyConnectedComponent();
    StronglyConnectedComponent sCC2 = new StronglyConnectedComponent();
    StronglyConnectedComponent sCC3 = new StronglyConnectedComponent();
    WeaklyConnectedComponent wCC1 = new WeaklyConnectedComponent();
    WeaklyConnectedComponent wCC2 = new WeaklyConnectedComponent();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeD);
    edge3.setSource(nodeD);
    edge3.setTarget(nodeC);
    edge4.setSource(nodeD);
    edge4.setTarget(nodeE);
    edge5.setSource(nodeE);
    edge5.setTarget(nodeD);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge3);
    nodeD.getOutgoingEdges().addAll(List.of(edge3, edge4));
    nodeD.getIncomingEdges().addAll(List.of(edge2, edge5));
    nodeE.getOutgoingEdges().add(edge5);
    nodeE.getIncomingEdges().add(edge4);
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
    sCC1.getNodesIds().add(nodeA.getId());
    sCC2.getNodesIds().add(nodeB.getId());
    sCC2.getExternalEdgesIds().add(edge1.getId());
    sCC3.getNodesIds().addAll(List.of(nodeC.getId(), nodeD.getId(), nodeE.getId()));
    sCC3.getExternalEdgesIds().addAll(List.of(edge2.getId(), edge3.getId(), edge4.getId(), edge5.getId()));
    wCC1.getNodesIds().addAll(List.of(nodeA.getId(), nodeB.getId()));
    wCC2.getNodesIds().addAll(List.of(nodeC.getId(), nodeD.getId(), nodeE.getId()));
    MapConnectivityCorrector corrector = new MapConnectivityCorrector(
        List.of(sCC1, sCC2, sCC3), List.of(wCC1, wCC2),
        new AllBridgesConnectFixer(new DirectBridgesConnectFixer(new StandardEdgeReflector()), new IndirectBridgesConnectFixer())
    );

    //then
    corrector.correct(graph);
    Assertions.assertEquals(2, nodeB.getIncomingEdges().size());
    Assertions.assertEquals(2, nodeB.getOutgoingEdges().size());
    Assertions.assertEquals(1, nodeA.getIncomingEdges().size());
    Assertions.assertEquals(3, nodeD.getIncomingEdges().size());
    Assertions.assertEquals(3, nodeD.getOutgoingEdges().size());
    Assertions.assertTrue(nodeD.getData().isCrossroad());
  }
}
