package pl.edu.agh.hiputs.partition.mapper.corrector.independent;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.priority.StandardPriorityProcessorChain;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class RoadsPriorityCorrectorTest {

  private final RoadsPriorityCorrector corrector = new RoadsPriorityCorrector(new StandardPriorityProcessorChain(
      List.of(edges -> edges.isEmpty() ? Optional.empty() : Optional.of(edges.get(0)))));

  @Test
  public void emptyGraph() {
    // given

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().build();

    // then
    Graph<JunctionData, WayData> resultGraph = corrector.correct(graph);
    Assertions.assertTrue(resultGraph.getEdges().values().stream().noneMatch(edge -> edge.getData().isPriorityRoad()));
  }

  @Test
  public void singleNode() {
    // given
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(true).build());

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA).build();

    // then
    Graph<JunctionData, WayData> resultGraph = corrector.correct(graph);
    Assertions.assertTrue(resultGraph.getEdges().values().stream().noneMatch(edge -> edge.getData().isPriorityRoad()));
  }

  @Test
  public void twoBendNodesWithOneEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();

    // then
    Graph<JunctionData, WayData> resultGraph = corrector.correct(graph);
    Assertions.assertTrue(resultGraph.getEdges().values().stream().noneMatch(edge -> edge.getData().isPriorityRoad()));
  }

  @Test
  public void twoBendOneCrossroadWithOneEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();

    // then
    Graph<JunctionData, WayData> resultGraph = corrector.correct(graph);
    Assertions.assertTrue(resultGraph.getEdges().values().stream().allMatch(edge -> edge.getData().isPriorityRoad()));
  }

  @Test
  public void threeNodesOneCommonCrossroadTwoEdges() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeB);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    Graph<JunctionData, WayData> resultGraph = corrector.correct(graph);
    Assertions.assertTrue(resultGraph.getEdges().values().stream().anyMatch(edge -> edge.getData().isPriorityRoad()));
  }

  @Test
  public void threeBendNodesTwoEdges() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeC);
    edge2.setTarget(nodeB);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    Graph<JunctionData, WayData> resultGraph = corrector.correct(graph);
    Assertions.assertTrue(resultGraph.getEdges().values().stream().noneMatch(edge -> edge.getData().isPriorityRoad()));
  }
}
