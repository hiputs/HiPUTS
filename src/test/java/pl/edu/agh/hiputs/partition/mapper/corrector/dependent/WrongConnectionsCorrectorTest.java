package pl.edu.agh.hiputs.partition.mapper.corrector.dependent;

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
import pl.edu.agh.hiputs.partition.model.graph.Node;

public class WrongConnectionsCorrectorTest {

  @Test
  public void happyPath() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).length(2.0).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("highway", "motorway");
    }}).length(3.0).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("highway", "primary");
    }}).length(2.0).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().lat(1.0).lon(1.0).build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().lat(2.0).lon(2.0).build());
    Node<JunctionData, WayData> nodeC = new Node<>("C", JunctionData.builder().lat(3.0).lon(3.0).build());
    Node<JunctionData, WayData> nodeD = new Node<>("D", JunctionData.builder().lat(4.0).lon(4.0).build());
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
    Graph<JunctionData, WayData> graph = new Graph.GraphBuilder<JunctionData, WayData>().addNode(nodeA)
        .addNode(nodeB)
        .addNode(nodeC)
        .addNode(nodeD)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();
    WrongConnectionsCorrector corrector =
        new WrongConnectionsCorrector(List.of(typeIncompatibility1, typeIncompatibility2), new AddNewEdgeTypesFixer());

    // then
    Graph<JunctionData, WayData> resultGraph = corrector.correct(graph);
    Assertions.assertEquals(6, resultGraph.getNodes().size());
  }
}
