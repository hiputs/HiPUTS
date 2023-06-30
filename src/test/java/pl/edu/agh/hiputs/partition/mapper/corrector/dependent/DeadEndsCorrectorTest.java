package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end.AddReversesOnDeadEndsFixer;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end.RemoveDeadEndsFixer;
import pl.edu.agh.hiputs.partition.mapper.helper.service.edge.reflector.StandardEdgeReflector;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class DeadEndsCorrectorTest {

  @Test
  public void happyPathWithRemovalFixerOnOutgoingDeadEnds() {
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
    List<DeadEnd> deadEnds = List.of(new DeadEnd(nodeD, List.of(edge2, edge3)));
    DeadEndsCorrector corrector = new DeadEndsCorrector(deadEnds, new RemoveDeadEndsFixer());

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(3, newGraph.getNodes().size());
    Assertions.assertEquals(3, newGraph.getEdges().size());
    Assertions.assertEquals(1, nodeA.getIncomingEdges().size());
  }

  @Test
  public void happyPathWithAddingReversesFixerOnIncomingDeadEnds() {
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
    List<DeadEnd> deadEnds = List.of(new DeadEnd(nodeB, List.of(edge1)));
    DeadEndsCorrector corrector = new DeadEndsCorrector(deadEnds,
        new AddReversesOnDeadEndsFixer(new StandardEdgeReflector()));

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(5, newGraph.getNodes().size());
    Assertions.assertEquals(6, newGraph.getEdges().size());
    Assertions.assertEquals(2, nodeA.getOutgoingEdges().size());
    Assertions.assertTrue(nodeA.getData().isCrossroad());
  }
}
