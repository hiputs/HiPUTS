package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

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

public class ComplexCrossroadsCorrectorTest {

  @Test
  public void emptyAll() {
    // given

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().build();
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of());

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertTrue(newGraph.getNodes().isEmpty());
    Assertions.assertTrue(newGraph.getEdges().isEmpty());
  }

  @Test
  public void emptyGraph() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());
    ComplexCrossroad complexCrossroad = new ComplexCrossroad();

    // when
    complexCrossroad.addNode(nodeA.getId());
    complexCrossroad.addNode(nodeB.getId());
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().build();
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of(complexCrossroad));

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertTrue(newGraph.getNodes().isEmpty());
    Assertions.assertTrue(newGraph.getEdges().isEmpty());
  }

  @Test
  public void emptyList() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addEdge(edge1)
        .build();
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of());

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(graph.getNodes().size(), newGraph.getNodes().size());
    Assertions.assertEquals(graph.getEdges().size(), newGraph.getEdges().size());
  }

  @Test
  public void singleNodeAsComplexCrossroad() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(21.0)
        .lon(30.0)
        .build());
    ComplexCrossroad complexCrossroad = new ComplexCrossroad();

    // when
    complexCrossroad.addNode(nodeA.getId());
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .build();
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of(complexCrossroad));

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(graph.getNodes().size(), newGraph.getNodes().size());
    Assertions.assertEquals(graph.getEdges().size(), newGraph.getEdges().size());
    Assertions.assertNotEquals(nodeA, newGraph.getNodes().values().stream().toList().get(0));
  }

  @Test
  public void twoNodesAsComplexCrossroads() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(21.0)
        .lon(30.0)
        .build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(20.0)
        .lon(31.0)
        .build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();
    ComplexCrossroad complexCrossroad2 = new ComplexCrossroad();

    // when
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad2.addNode(nodeB.getId());
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .build();
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of(complexCrossroad1, complexCrossroad2));

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(graph.getNodes().size(), newGraph.getNodes().size());
    Assertions.assertEquals(graph.getEdges().size(), newGraph.getEdges().size());
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeA.getId()));
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeB.getId()));
  }

  @Test
  public void twoNodesAsOneComplexCrossroadWithEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(21.0)
        .lon(30.0)
        .build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(20.0)
        .lon(31.0)
        .build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addEdge(edge1)
        .build();
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of(complexCrossroad1));

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(1, newGraph.getNodes().size());
    Assertions.assertEquals(0, newGraph.getEdges().size());
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeA.getId()));
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeB.getId()));
  }

  @Test
  public void twoNodesAndOnlyOneComplexCrossroadWithEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(21.0)
        .lon(30.0)
        .build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(20.0)
        .lon(31.0)
        .build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    complexCrossroad1.addNode(nodeA.getId());
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addEdge(edge1)
        .build();
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of(complexCrossroad1));

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(2, newGraph.getNodes().size());
    Assertions.assertEquals(1, newGraph.getEdges().size());
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeA.getId()));
    Assertions.assertTrue(newGraph.getNodes().containsKey(nodeB.getId()));
  }

  @Test
  public void twoNodesAsTwoSeparateComplexCrossroadsWithEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(21.0)
        .lon(30.0)
        .build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(20.0)
        .lon(31.0)
        .build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();
    ComplexCrossroad complexCrossroad2 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad2.addNode(nodeB.getId());
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addEdge(edge1)
        .build();
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of(complexCrossroad1, complexCrossroad2));

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(2, newGraph.getNodes().size());
    Assertions.assertEquals(1, newGraph.getEdges().size());
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeA.getId()));
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeB.getId()));
  }

  @Test
  public void threeNodesWithTwoComplexCrossroadsAndBendBetween() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(21.0)
        .lon(30.0)
        .build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(20.0)
        .lon(31.0)
        .build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(21.0)
        .lon(31.0)
        .build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();
    ComplexCrossroad complexCrossroad2 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeB.getIncomingEdges().add(edge2);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad2.addNode(nodeC.getId());
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of(complexCrossroad1, complexCrossroad2));

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(3, newGraph.getNodes().size());
    Assertions.assertEquals(2, newGraph.getEdges().size());
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeA.getId()));
    Assertions.assertTrue(newGraph.getNodes().containsKey(nodeB.getId()));
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeC.getId()));
  }

  @Test
  public void threeNodesWithTwoComplexCrossroadsOverall() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(21.0)
        .lon(30.0)
        .build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(20.0)
        .lon(31.0)
        .build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(21.0)
        .lon(31.0)
        .build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();
    ComplexCrossroad complexCrossroad2 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeB.getIncomingEdges().add(edge2);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    complexCrossroad2.addNode(nodeC.getId());
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of(complexCrossroad1, complexCrossroad2));

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(2, newGraph.getNodes().size());
    Assertions.assertEquals(1, newGraph.getEdges().size());
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeA.getId()));
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeB.getId()));
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeC.getId()));
  }

  @Test
  public void fiveNodesWithTwoComplexCrossroadsAndBend() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(21.0)
        .lon(30.0)
        .build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(20.0)
        .lon(30.0)
        .build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(21.0)
        .lon(31.0)
        .build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(20.0)
        .lon(31.0)
        .build());
    Node<JunctionData, WayData> nodeE = new Node<>("E", JunctionData.builder()
        .tags(new HashMap<>())
        .lat(22.0)
        .lon(32.0)
        .build());
    ComplexCrossroad complexCrossroad1 = new ComplexCrossroad();
    ComplexCrossroad complexCrossroad2 = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeD);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeD);
    edge4.setSource(nodeA);
    edge4.setTarget(nodeE);
    edge5.setSource(nodeE);
    edge5.setTarget(nodeC);
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge4));
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeD.getIncomingEdges().addAll(List.of(edge2, edge3));
    nodeC.getIncomingEdges().add(edge5);
    nodeC.getOutgoingEdges().add(edge3);
    nodeE.getIncomingEdges().add(edge4);
    nodeE.getOutgoingEdges().add(edge5);
    complexCrossroad1.addNode(nodeA.getId());
    complexCrossroad1.addNode(nodeB.getId());
    complexCrossroad2.addNode(nodeC.getId());
    complexCrossroad2.addNode(nodeD.getId());
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
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
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of(complexCrossroad1, complexCrossroad2));

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(3, newGraph.getNodes().size());
    Assertions.assertEquals(3, newGraph.getEdges().size());
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeA.getId()));
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeB.getId()));
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeC.getId()));
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeD.getId()));
    Assertions.assertTrue(newGraph.getNodes().containsKey(nodeE.getId()));
  }

  @Test
  public void complexGraph() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(1).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).length(1).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge4 = new Edge<>("4", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge5 = new Edge<>("5", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge6 = new Edge<>("6", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge7 = new Edge<>("7", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge8 = new Edge<>("8", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge9 = new Edge<>("9", WayData.builder().tags(new HashMap<>()).length(4).build());
    Edge<JunctionData, WayData> edge10 = new Edge<>("10", WayData.builder().tags(new HashMap<>()).length(4).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .tags(new HashMap<>()).lat(50.0945494).lon(19.8775622).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .tags(new HashMap<>()).lat(50.0942335).lon(19.8788777).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder()
        .tags(new HashMap<>()).lat(50.0932701).lon(19.8772253).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder()
        .tags(new HashMap<>()).lat(50.0956796).lon(19.8784069).build());
    ComplexCrossroad complexCrossroad = new ComplexCrossroad();

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeA);
    edge3.setSource(nodeA);
    edge3.setTarget(nodeC);
    edge4.setSource(nodeC);
    edge4.setTarget(nodeA);
    edge5.setSource(nodeB);
    edge5.setTarget(nodeC);
    edge6.setSource(nodeC);
    edge6.setTarget(nodeB);
    edge7.setSource(nodeB);
    edge7.setTarget(nodeD);
    edge8.setSource(nodeD);
    edge8.setTarget(nodeB);
    edge9.setSource(nodeA);
    edge9.setTarget(nodeD);
    edge10.setSource(nodeD);
    edge10.setTarget(nodeA);
    nodeA.getIncomingEdges().addAll(List.of(edge2, edge4, edge10));
    nodeA.getOutgoingEdges().addAll(List.of(edge1, edge3, edge9));
    nodeB.getIncomingEdges().addAll(List.of(edge1, edge6, edge8));
    nodeB.getOutgoingEdges().addAll(List.of(edge2, edge7, edge5));
    nodeC.getIncomingEdges().addAll(List.of(edge3, edge5));
    nodeC.getOutgoingEdges().addAll(List.of(edge4, edge6));
    nodeD.getIncomingEdges().addAll(List.of(edge7, edge9));
    nodeD.getOutgoingEdges().addAll(List.of(edge8, edge10));
    complexCrossroad.addNode(nodeA.getId());
    complexCrossroad.addNode(nodeB.getId());
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .addEdge(edge4)
        .addEdge(edge5)
        .addEdge(edge6)
        .addEdge(edge7)
        .addEdge(edge8)
        .addEdge(edge9)
        .addEdge(edge10)
        .build();
    ComplexCrossroadsCorrector corrector = new ComplexCrossroadsCorrector(List.of(complexCrossroad));

    // then
    Graph<JunctionData, WayData> newGraph = corrector.correct(graph);
    Assertions.assertEquals(3, newGraph.getNodes().size());
    Assertions.assertEquals(4, newGraph.getEdges().size());
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeA.getId()));
    Assertions.assertFalse(newGraph.getNodes().containsKey(nodeB.getId()));
    Assertions.assertTrue(newGraph.getNodes().containsKey(nodeC.getId()));
    Assertions.assertTrue(newGraph.getNodes().containsKey(nodeD.getId()));
  }
}
