package pl.edu.agh.hiputs.partition.mapper.detector.util.compatibility;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.hiputs.partition.mapper.helper.structure.compatibility.TypeIncompatibility;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class OnMotorwaysIncompatibilityFinderTest {

  private final OnMotorwaysIncompatibilityFinder finder = new OnMotorwaysIncompatibilityFinder();

  @Test
  public void emptyGraph() {
    // given

    // when
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void singleNoMotorwayEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void singleMotorwayEdge() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void twoMotorwayEdges() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void twoEdgesOneMotorwayOneLinkAndCrossroadBetween() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway_link");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void twoEdgesOneMotorwayOneLinkAndBendBetween() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway_link");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void twoEdgesOneLinkOneMotorwayAndCrossroadBetween() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway_link");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void twoEdgesOneLinkOneMotorwayAndBendBetween() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway_link");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void twoEdgesOneMotorwayOnePrimaryAndCrossroadBetween() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void twoEdgesOneMotorwayOnePrimaryAndBendBetween() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertEquals(1, typeIncompatibilities.size());
    Assertions.assertEquals(edge2, typeIncompatibilities.get(0).getImpactedEdge());
  }

  @Test
  public void twoEdgesOnePrimaryOneMotorwayAndCrossroadBetween() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void twoEdgesOnePrimaryOneMotorwayAndBendBetween() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertEquals(1, typeIncompatibilities.size());
    Assertions.assertEquals(edge1, typeIncompatibilities.get(0).getImpactedEdge());
  }

  @Test
  public void twoPrimaryEdges() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    edge2.setSource(nodeB);
    edge2.setTarget(nodeC);
    nodeA.getOutgoingEdges().add(edge1);
    nodeB.getIncomingEdges().add(edge1);
    nodeB.getOutgoingEdges().add(edge2);
    nodeC.getIncomingEdges().add(edge2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addEdge(edge1)
        .addEdge(edge2)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void threeEdgesOneMotorwayTwoCrossroads() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().isCrossroad(false).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void threeEdgesOneMotorwayOneCrossroads() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().isCrossroad(false).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertEquals(1, typeIncompatibilities.size());
    Assertions.assertEquals(edge1, typeIncompatibilities.get(0).getImpactedEdge());
  }

  @Test
  public void threeEdgesOneMotorwayAndBends() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().isCrossroad(false).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertEquals(2, typeIncompatibilities.size());
  }

  @Test
  public void threeEdgesTwoMotorwaysTwoCrossroads() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().isCrossroad(false).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }

  @Test
  public void threeEdgesTwoMotorwaysOneCrossroad() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(true).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().isCrossroad(false).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertEquals(1, typeIncompatibilities.size());
    Assertions.assertEquals(edge2, typeIncompatibilities.get(0).getImpactedEdge());
  }

  @Test
  public void threeEdgesTwoMotorwaysAndBends() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().isCrossroad(false).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertEquals(1, typeIncompatibilities.size());
  }

  @Test
  public void threeEdgesThreeMotorways() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().isCrossroad(false).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().isCrossroad(false).build());

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
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // then
    List<TypeIncompatibility> typeIncompatibilities = finder.lookup(graph);
    Assertions.assertTrue(typeIncompatibilities.isEmpty());
  }
}
