package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.type.end;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.end.RemoveDeadEndsFixer;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.end.DeadEnd;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class RemoveDeadEndsFixerTest {

  private final RemoveDeadEndsFixer fixer = new RemoveDeadEndsFixer();

  @Test
  public void singleNodeGraphWithOneDeadEndWithoutEdges() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());

    // when
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).build();
    List<DeadEnd> deadEnds = List.of(new DeadEnd(nodeA, List.of()));

    // then
    Graph<JunctionData, WayData> newGraph = fixer.fixFoundDeadEnds(deadEnds, graph);
    Assertions.assertEquals(1, newGraph.getNodes().size());
    Assertions.assertTrue(newGraph.getEdges().isEmpty());
  }

  @Test
  public void twoNodesGraphWithOneDeadEndWithoutEdges() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());

    // when
    Graph<JunctionData, WayData> graph =
        new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).build();
    List<DeadEnd> deadEnds = List.of(new DeadEnd(nodeA, List.of()));

    // then
    Graph<JunctionData, WayData> newGraph = fixer.fixFoundDeadEnds(deadEnds, graph);
    Assertions.assertEquals(2, newGraph.getNodes().size());
    Assertions.assertTrue(newGraph.getEdges().isEmpty());
  }

  @Test
  public void twoNodesGraphWithOneFullDeadEnds() {
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
    List<DeadEnd> deadEnds = List.of(new DeadEnd(nodeA, List.of(edge1)));

    // then
    Graph<JunctionData, WayData> newGraph = fixer.fixFoundDeadEnds(deadEnds, graph);
    Assertions.assertTrue(newGraph.getNodes().isEmpty());
    Assertions.assertTrue(newGraph.getEdges().isEmpty());
  }

  @Test
  public void threeNodesGraphWithOneFullDeadEnds() {
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
    List<DeadEnd> deadEnds = List.of(new DeadEnd(nodeA, List.of(edge1, edge2)));

    // then
    Graph<JunctionData, WayData> newGraph = fixer.fixFoundDeadEnds(deadEnds, graph);
    Assertions.assertTrue(newGraph.getNodes().isEmpty());
    Assertions.assertTrue(newGraph.getEdges().isEmpty());
  }

  @Test
  public void threeNodesGraphWithOneDeadEndsAndOneEdge() {
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
    edge2.setSource(nodeA);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeA);
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge2));
    nodeA.getIncomingEdges().add(edge3);
    nodeB.getIncomingEdges().add(edge1);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    List<DeadEnd> deadEnds = List.of(new DeadEnd(nodeB, List.of(edge1)));

    // then
    Graph<JunctionData, WayData> newGraph = fixer.fixFoundDeadEnds(deadEnds, graph);
    Assertions.assertEquals(2, newGraph.getNodes().size());
    Assertions.assertEquals(2, newGraph.getEdges().size());
  }

  @Test
  public void threeNodesGraphWithTwoDeadEnds() {
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
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();
    List<DeadEnd> deadEnds = List.of(new DeadEnd(nodeB, List.of(edge1)), new DeadEnd(nodeC, List.of(edge2)));

    // then
    Graph<JunctionData, WayData> newGraph = fixer.fixFoundDeadEnds(deadEnds, graph);
    Assertions.assertTrue(newGraph.getNodes().isEmpty());
    Assertions.assertTrue(newGraph.getEdges().isEmpty());
  }
}
