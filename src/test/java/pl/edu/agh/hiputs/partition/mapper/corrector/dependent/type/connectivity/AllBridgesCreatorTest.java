package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.type.connectivity;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.AllBridgesCreator;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.DirectedBridgesCreator;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.connectivity.UndirectedBridgesCreator;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.StronglyConnectedComponent;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.connectivity.WeaklyConnectedComponent;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class AllBridgesCreatorTest {
  private final AllBridgesCreator creator = new AllBridgesCreator(
      new DirectedBridgesCreator(), new UndirectedBridgesCreator()
  );

  @Test
  public void happyPath() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("A->B", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("C->D", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("D->C", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .lon(50.1254876).lat(19.9106595).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .lon(50.1257849).lat(19.9104581).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder()
        .lon(50.1275802).lat(19.9123953).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder()
        .lon(50.1274453).lat(19.9121929).build());
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
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge3);
    nodeD.getOutgoingEdges().add(edge3);
    nodeD.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    sCC1.getNodesIds().add(nodeA.getId());
    sCC2.getNodesIds().add(nodeB.getId());
    sCC2.getExternalEdgesIds().add(edge1.getId());
    sCC3.getNodesIds().addAll(List.of(nodeC.getId(), nodeD.getId()));
    wCC1.getNodesIds().addAll(List.of(nodeA.getId(), nodeB.getId()));
    wCC2.getNodesIds().addAll(List.of(nodeC.getId(), nodeD.getId()));

    // then
    creator.createBetweenCCsOnGraph(
        List.of(sCC1, sCC2, sCC3), List.of(wCC1, wCC2), graph
    );
    Assertions.assertEquals(2, nodeB.getIncomingEdges().size());
    Assertions.assertEquals(2, nodeB.getOutgoingEdges().size());
    Assertions.assertEquals(1, nodeA.getIncomingEdges().size());
    Assertions.assertEquals(2, nodeD.getIncomingEdges().size());
    Assertions.assertEquals(2, nodeD.getOutgoingEdges().size());
  }
}
