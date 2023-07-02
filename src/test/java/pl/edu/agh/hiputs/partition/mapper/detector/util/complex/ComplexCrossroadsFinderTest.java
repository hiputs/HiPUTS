package pl.edu.agh.hiputs.partition.mapper.detector.util.complex;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.complex.ComplexCrossroad;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@ExtendWith(MockitoExtension.class)
public class ComplexCrossroadsFinderTest {
  private final ModelConfigurationService modelConfigurationService =
      Mockito.mock(ModelConfigurationService.class, Mockito.RETURNS_DEEP_STUBS);
  private final ComplexCrossroadsFinder finder = new ComplexCrossroadsFinder(modelConfigurationService);

  @Test
  public void emptyGraph() {
    // given

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void singleNoCrossroadNode() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void singleCrossroadNode() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .build();

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void twoNoCrossroadNodesWithEdgeGreaterThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(3.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());

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
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(2.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void twoNoCrossroadNodesWithEdgeLowerThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(3.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());

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
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(4.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void twoCrossroadNodesWithEdgeGreaterThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(3.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());

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
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(2.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void twoCrossroadNodesWithEdgeLowerThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(3.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());

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
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(4.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertEquals(1, complexCrossroads.size());
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeA.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeB.getId()));
  }

  @Test
  public void twoNodesOneCrossroadWithEdgeGreaterThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(3.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());

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
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(2.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void twoNodesOneCrossroadWithEdgeLowerThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(3.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());

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
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(4.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void threeNodesAllCrossroadWithEdgesGreaterThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(3.0).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).length(4.0).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).length(4.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(true).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(2.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void threeNodesAllCrossroadWithTwoEdgesGreaterThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(3.0).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).length(4.0).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(true).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(2.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertEquals(1, complexCrossroads.size());
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeA.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeC.getId()));
  }

  @Test
  public void threeNodesAllCrossroadWithOneEdgeGreaterThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(3.0).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(true).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(2.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertEquals(1, complexCrossroads.size());
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeA.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeB.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeC.getId()));
  }

  @Test
  public void threeNodesAllCrossroadWithZeroEdgesGreaterThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(true).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(2.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertEquals(1, complexCrossroads.size());
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeA.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeB.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeC.getId()));
  }

  @Test
  public void threeNodesTwoCrossroadWithAllEdgesLowerThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(2.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertEquals(1, complexCrossroads.size());
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeA.getId()));
    Assertions.assertTrue(complexCrossroads.get(0).getNodesIdsIn().contains(nodeB.getId()));
  }

  @Test
  public void threeNodesOneCrossroadWithAllEdgesLowerThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(2.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }

  @Test
  public void threeNodesZeroCrossroadWithAllEdgesLowerThan() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>()).length(1.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    Mockito.when(modelConfigurationService.getModelConfig().getCrossroadMinDistance()).thenReturn(2.0);

    // then
    List<ComplexCrossroad> complexCrossroads = finder.lookup(graph);
    Assertions.assertTrue(complexCrossroads.isEmpty());
  }
}
