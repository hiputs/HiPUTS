package pl.edu.agh.hiputs.partition.mapper.corrector.dependent.type.compatibility;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.corrector.dependent.strategy.type.compatibility.AddNewEdgeTypesFixer;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.compatibility.TypeIncompatibility;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class AddNewEdgeTypesFixerTest {
  private final AddNewEdgeTypesFixer fixer = new AddNewEdgeTypesFixer();

  @Test
  public void emptyGraphEmptyList() {
    // given

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().build();

    // then
    Graph<JunctionData, WayData> resultGraph = fixer.fixFoundIncompatibilities(List.of(), graph);
    Assertions.assertTrue(resultGraph.getNodes().isEmpty());
    Assertions.assertTrue(resultGraph.getEdges().isEmpty());
  }

  @Test
  public void emptyGraph() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary");}}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    TypeIncompatibility typeIncompatibility = new TypeIncompatibility("motorway", "motorway_link", edge1);

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().build();

    // then
    Graph<JunctionData, WayData> resultGraph = fixer.fixFoundIncompatibilities(List.of(typeIncompatibility), graph);
    Assertions.assertTrue(resultGraph.getNodes().isEmpty());
    Assertions.assertTrue(resultGraph.getEdges().isEmpty());
  }

  @Test
  public void emptyList() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "motorway");}}).build());
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

    // then
    Graph<JunctionData, WayData> resultGraph = fixer.fixFoundIncompatibilities(List.of(), graph);
    Assertions.assertEquals(2, resultGraph.getNodes().size());
    Assertions.assertEquals(1, resultGraph.getEdges().size());
    Assertions.assertEquals("motorway", edge1.getData().getTags().get("highway"));
  }

  @Test
  public void oneEdgeWithIncompatibility() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary");}}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .isCrossroad(false).lat(1.0).lon(1.0).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .isCrossroad(false).lat(2.0).lon(2.0).build());
    TypeIncompatibility typeIncompatibility = new TypeIncompatibility("motorway", "motorway_link", edge1);

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

    // then
    Graph<JunctionData, WayData> resultGraph = fixer.fixFoundIncompatibilities(List.of(typeIncompatibility), graph);
    Assertions.assertEquals(2, resultGraph.getNodes().size());
    Assertions.assertEquals(1, resultGraph.getEdges().size());
    Assertions.assertEquals("primary", edge1.getData().getTags().get("highway"));
  }

  @Test
  public void twoEdgesWithListNoMatching() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary");}}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary");}}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    TypeIncompatibility typeIncompatibility = new TypeIncompatibility("motorway", "motorway_link", edge2);

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeA);
    nodeA.getOutgoingEdges().add(edge1);
    nodeA.getIncomingEdges().add(edge2);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addEdge(edge1)
        .build();

    // then
    Graph<JunctionData, WayData> resultGraph = fixer.fixFoundIncompatibilities(List.of(typeIncompatibility), graph);
    Assertions.assertEquals(2, resultGraph.getNodes().size());
    Assertions.assertEquals(1, resultGraph.getEdges().size());
    Assertions.assertEquals("primary", edge1.getData().getTags().get("highway"));
    Assertions.assertEquals("primary", edge2.getData().getTags().get("highway"));
  }

  @Test
  public void threeEdgesAndTwoSameIncompatibilities() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "motorway");}}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary");}}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "motorway");}}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .isCrossroad(false).lat(1.0).lon(1.0).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .isCrossroad(false).lat(1.0).lon(4.0).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder()
        .isCrossroad(false).lat(1.0).lon(7.0).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder()
        .isCrossroad(false).lat(1.0).lon(10.0).build());
    TypeIncompatibility typeIncompatibility1 = new TypeIncompatibility("motorway", "motorway_link", edge2);
    TypeIncompatibility typeIncompatibility2 = new TypeIncompatibility("motorway", "motorway_link", edge2);

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeD);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    nodeD.getIncomingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    Graph<JunctionData, WayData> resultGraph = fixer.fixFoundIncompatibilities(
        List.of(typeIncompatibility1, typeIncompatibility2), graph);
    Assertions.assertEquals(6, resultGraph.getNodes().size());
    Assertions.assertEquals(5, resultGraph.getEdges().size());
    Assertions.assertEquals("motorway", edge1.getData().getTags().get("highway"));
    Assertions.assertEquals("primary", edge2.getData().getTags().get("highway"));
    Assertions.assertEquals("motorway", edge3.getData().getTags().get("highway"));
    Assertions.assertEquals(2, resultGraph.getEdges().values().stream()
        .filter(edge -> edge.getData().getTags().containsKey("highway") &&
            edge.getData().getTags().get("highway").equals("motorway_link"))
        .count());
  }

  @Test
  public void threeEdgesAndTwoDifferentIncompatibilities() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary");}}).length(3.0).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "motorway");}}).length(3.0).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary");}}).length(3.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder()
        .isCrossroad(false).lat(1.0).lon(1.0).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder()
        .isCrossroad(false).lat(1.0).lon(4.0).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder()
        .isCrossroad(false).lat(1.0).lon(7.0).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder()
        .isCrossroad(false).lat(1.0).lon(10.0).build());
    TypeIncompatibility typeIncompatibility1 = new TypeIncompatibility("motorway", "motorway_link", edge1);
    TypeIncompatibility typeIncompatibility2 = new TypeIncompatibility("motorway", "motorway_link", edge3);

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeD);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    nodeD.getIncomingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    Graph<JunctionData, WayData> resultGraph = fixer.fixFoundIncompatibilities(
        List.of(typeIncompatibility1, typeIncompatibility2), graph);
    Assertions.assertEquals(6, resultGraph.getNodes().size());
    Assertions.assertEquals(5, resultGraph.getEdges().size());
    Assertions.assertEquals("primary", edge1.getData().getTags().get("highway"));
    Assertions.assertEquals("motorway", edge2.getData().getTags().get("highway"));
    Assertions.assertEquals("primary", edge3.getData().getTags().get("highway"));
    Assertions.assertEquals(2, resultGraph.getEdges().values().stream()
        .filter(edge -> edge.getData().getTags().containsKey("highway") &&
            edge.getData().getTags().get("highway").equals("motorway_link"))
        .count());
  }

  @Test
  public void threeEdgesAndThreeDifferentIncompatibilities() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary");}}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary");}}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary");}}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().isCrossroad(false).build());
    TypeIncompatibility typeIncompatibility1 = new TypeIncompatibility("motorway", "motorway_link", edge1);
    TypeIncompatibility typeIncompatibility2 = new TypeIncompatibility("motorway", "motorway_link", edge2);
    TypeIncompatibility typeIncompatibility3 = new TypeIncompatibility("motorway", "motorway_link", edge3);

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    edge3.setSource(nodeC);
    edge3.setTarget(nodeD);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    nodeC.getOutgoingEdges().add(edge3);
    nodeD.getIncomingEdges().add(edge3);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>()
        .addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    Graph<JunctionData, WayData> resultGraph = fixer.fixFoundIncompatibilities(
        List.of(typeIncompatibility1, typeIncompatibility2, typeIncompatibility3), graph);
    Assertions.assertEquals(4, resultGraph.getNodes().size());
    Assertions.assertEquals(3, resultGraph.getEdges().size());
    Assertions.assertEquals("primary", edge1.getData().getTags().get("highway"));
    Assertions.assertEquals("primary", edge2.getData().getTags().get("highway"));
    Assertions.assertEquals("primary", edge3.getData().getTags().get("highway"));
  }
}
