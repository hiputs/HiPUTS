package pl.edu.agh.hiputs.partition.mapper.corrector.independent;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.sort.ByAngleEdgeSorter;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class RoadsOrderCorrectorTest {
  private final RoadsOrderCorrector corrector = new RoadsOrderCorrector(new ByAngleEdgeSorter());

  @Test
  public void sortEmptyGraph() {
    // given

    // when
    Graph<JunctionData, WayData> returned = corrector.correct(new GraphBuilder<JunctionData, WayData>().build());
    List<Edge<JunctionData, WayData>> incomingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getIncomingEdges)
        .orElse(Collections.emptyList());
    List<Edge<JunctionData, WayData>> outgoingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getOutgoingEdges)
        .orElse(Collections.emptyList());

    // then
    Assertions.assertTrue(incomingOrder.isEmpty());
    Assertions.assertTrue(outgoingOrder.isEmpty());
  }

  @Test
  public void sortNoEdges() {
    // given
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).tags(Collections.emptyMap()).lat(50.0850637).lon(19.8911986).build());
    Node<JunctionData, WayData> nextNode1 = new Node<>("1",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0843471).lon(19.8911159).build());
    Node<JunctionData, WayData> nextNode2 = new Node<>("2",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0852816).lon(19.8912199).build());
    Node<JunctionData, WayData> nextNode3 = new Node<>("3",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0852747).lon(19.8913083).build());

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(crossroad)
        .addNode(nextNode1)
        .addNode(nextNode2)
        .addNode(nextNode3)
        .build();
    Graph<JunctionData, WayData> returned = corrector.correct(graph);
    List<Edge<JunctionData, WayData>> incomingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getIncomingEdges)
        .orElse(Collections.emptyList());
    List<Edge<JunctionData, WayData>> outgoingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getOutgoingEdges)
        .orElse(Collections.emptyList());

    // then
    Assertions.assertTrue(incomingOrder.isEmpty());
    Assertions.assertTrue(outgoingOrder.isEmpty());
  }

  @Test
  public void sortSingleEdges() {
    // given
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).tags(Collections.emptyMap()).lat(50.0850637).lon(19.8911986).build());
    Node<JunctionData, WayData> nextNode1 = new Node<>("1",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0843471).lon(19.8911159).build());
    Edge<JunctionData, WayData> inEdge1 = new Edge<>("10",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("01",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());

    // when
    inEdge1.setSource(nextNode1);
    inEdge1.setTarget(crossroad);
    outEdge1.setSource(crossroad);
    outEdge1.setTarget(nextNode1);
    crossroad.getIncomingEdges().add(inEdge1);
    crossroad.getOutgoingEdges().add(outEdge1);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(crossroad)
        .addEdge(inEdge1)
        .addEdge(outEdge1)
        .build();
    Graph<JunctionData, WayData> returned = corrector.correct(graph);
    List<Edge<JunctionData, WayData>> incomingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getIncomingEdges)
        .orElse(Collections.emptyList());
    List<Edge<JunctionData, WayData>> outgoingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getOutgoingEdges)
        .orElse(Collections.emptyList());

    // then
    Assertions.assertEquals(inEdge1, incomingOrder.get(0));
    Assertions.assertEquals(outEdge1, outgoingOrder.get(0));
  }

  @Test
  public void happyPathToSortOutgoingEdgesOnly() {
    // given
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).tags(Collections.emptyMap()).lat(50.0850637).lon(19.8911986).build());
    Node<JunctionData, WayData> nextNode1 = new Node<>("1",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0843471).lon(19.8911159).build());
    Node<JunctionData, WayData> nextNode2 = new Node<>("2",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0852816).lon(19.8912199).build());
    Node<JunctionData, WayData> nextNode3 = new Node<>("3",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0852747).lon(19.8913083).build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("01",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge2 = new Edge<>("02",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge3 = new Edge<>("03",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());

    // when
    outEdge1.setSource(crossroad);
    outEdge1.setTarget(nextNode1);
    outEdge2.setSource(crossroad);
    outEdge2.setTarget(nextNode2);
    outEdge3.setSource(crossroad);
    outEdge3.setTarget(nextNode3);
    crossroad.getOutgoingEdges().addAll(List.of(outEdge1, outEdge2, outEdge3));
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(crossroad)
        .addEdge(outEdge1)
        .addEdge(outEdge2)
        .addEdge(outEdge3)
        .build();
    Graph<JunctionData, WayData> returned = corrector.correct(graph);
    List<Edge<JunctionData, WayData>> incomingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getIncomingEdges)
        .orElse(Collections.emptyList());
    List<Edge<JunctionData, WayData>> outgoingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getOutgoingEdges)
        .orElse(Collections.emptyList());

    // then
    Assertions.assertTrue(incomingOrder.isEmpty());
    Assertions.assertEquals(outEdge1, outgoingOrder.get(0));
    Assertions.assertEquals(outEdge2, outgoingOrder.get(1));
    Assertions.assertEquals(outEdge3, outgoingOrder.get(2));
  }

  @Test
  public void happyPathToSortIncomingEdgesOnly() {
    // given
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).tags(Collections.emptyMap()).lat(50.0850637).lon(19.8911986).build());
    Node<JunctionData, WayData> nextNode1 = new Node<>("1",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0843471).lon(19.8911159).build());
    Node<JunctionData, WayData> nextNode2 = new Node<>("2",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0852816).lon(19.8912199).build());
    Node<JunctionData, WayData> nextNode3 = new Node<>("3",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0852747).lon(19.8913083).build());
    Edge<JunctionData, WayData> inEdge1 = new Edge<>("10",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> inEdge2 = new Edge<>("20",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> inEdge3 = new Edge<>("30",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());

    // when
    inEdge1.setSource(nextNode1);
    inEdge1.setTarget(crossroad);
    inEdge2.setSource(nextNode2);
    inEdge2.setTarget(crossroad);
    inEdge3.setSource(nextNode3);
    inEdge3.setTarget(crossroad);
    crossroad.getIncomingEdges().addAll(List.of(inEdge1, inEdge2, inEdge3));
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(crossroad)
        .addEdge(inEdge1)
        .addEdge(inEdge2)
        .addEdge(inEdge3)
        .build();
    Graph<JunctionData, WayData> returned = corrector.correct(graph);
    List<Edge<JunctionData, WayData>> incomingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getIncomingEdges)
        .orElse(Collections.emptyList());
    List<Edge<JunctionData, WayData>> outgoingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getOutgoingEdges)
        .orElse(Collections.emptyList());

    // then
    Assertions.assertEquals(inEdge1, incomingOrder.get(0));
    Assertions.assertEquals(inEdge2, incomingOrder.get(1));
    Assertions.assertEquals(inEdge3, incomingOrder.get(2));
    Assertions.assertTrue(outgoingOrder.isEmpty());
  }

  @Test
  public void happyPathToSortIncomingAndOutgoingEdges() {
    // given
    Node<JunctionData, WayData> crossroad = new Node<>("0",
        JunctionData.builder().isCrossroad(true).tags(Collections.emptyMap()).lat(50.0850637).lon(19.8911986).build());
    Node<JunctionData, WayData> nextNode1 = new Node<>("1",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0843471).lon(19.8911159).build());
    Node<JunctionData, WayData> nextNode2 = new Node<>("2",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0852816).lon(19.8912199).build());
    Node<JunctionData, WayData> nextNode3 = new Node<>("3",
        JunctionData.builder().isCrossroad(false).tags(Collections.emptyMap()).lat(50.0852747).lon(19.8913083).build());
    Edge<JunctionData, WayData> inEdge1 = new Edge<>("10",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> inEdge2 = new Edge<>("20",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> inEdge3 = new Edge<>("30",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge1 = new Edge<>("01",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge2 = new Edge<>("02",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());
    Edge<JunctionData, WayData> outEdge3 = new Edge<>("03",
        WayData.builder()
            .tags(Collections.emptyMap())
            .build());

    // when
    inEdge1.setSource(nextNode1);
    inEdge1.setTarget(crossroad);
    inEdge2.setSource(nextNode2);
    inEdge2.setTarget(crossroad);
    inEdge3.setSource(nextNode3);
    inEdge3.setTarget(crossroad);
    outEdge1.setSource(crossroad);
    outEdge1.setTarget(nextNode1);
    outEdge2.setSource(crossroad);
    outEdge2.setTarget(nextNode2);
    outEdge3.setSource(crossroad);
    outEdge3.setTarget(nextNode3);
    crossroad.getIncomingEdges().addAll(List.of(inEdge1, inEdge2, inEdge3));
    crossroad.getOutgoingEdges().addAll(List.of(outEdge1, outEdge2, outEdge3));
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(crossroad)
        .addEdge(inEdge1)
        .addEdge(inEdge2)
        .addEdge(inEdge3)
        .addEdge(outEdge1)
        .addEdge(outEdge2)
        .addEdge(outEdge3)
        .build();
    Graph<JunctionData, WayData> returned = corrector.correct(graph);
    List<Edge<JunctionData, WayData>> incomingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getIncomingEdges)
        .orElse(Collections.emptyList());
    List<Edge<JunctionData, WayData>> outgoingOrder = returned.getNodes().values().stream()
        .findFirst()
        .map(Node::getOutgoingEdges)
        .orElse(Collections.emptyList());

    // then
    Assertions.assertEquals(inEdge1, incomingOrder.get(0));
    Assertions.assertEquals(inEdge2, incomingOrder.get(1));
    Assertions.assertEquals(inEdge3, incomingOrder.get(2));
    Assertions.assertEquals(outEdge1, outgoingOrder.get(0));
    Assertions.assertEquals(outEdge2, outgoingOrder.get(1));
    Assertions.assertEquals(outEdge3, outgoingOrder.get(2));
  }
}
