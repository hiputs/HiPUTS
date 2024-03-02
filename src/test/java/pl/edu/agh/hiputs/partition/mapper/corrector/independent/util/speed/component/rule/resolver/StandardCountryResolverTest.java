package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.resolver;

import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@ExtendWith(MockitoExtension.class)
public class StandardCountryResolverTest {

  @InjectMocks
  private StandardCountryResolver countryResolver;

  @Test
  public void findValueInNodesWithoutRequiredCountryTag() {
    // given
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(
        new Node<>("1", JunctionData.builder().tags(Collections.emptyMap()).build())).build();

    // when
    countryResolver.deduceCountry(graph);

    // then
    Assertions.assertEquals("DEFAULT", countryResolver.getCountry());
  }

  @Test
  public void findValueInEdgesWithoutRequiredCountryTag() {
    // given
    Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder().tags(Collections.emptyMap()).build());
    Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder().tags(Collections.emptyMap()).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(Collections.emptyMap()).build());
    edge1.setSource(node1);
    edge1.setTarget(node2);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addEdge(edge1).build();

    // when
    countryResolver.deduceCountry(graph);

    // then
    Assertions.assertEquals("DEFAULT", countryResolver.getCountry());
  }

  @Test
  public void findValueInObjectsWithoutRequiredCountryTag() {
    // given
    Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder().tags(Collections.emptyMap()).build());
    Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder().tags(Collections.emptyMap()).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(Collections.emptyMap()).build());
    edge1.setSource(node1);
    edge1.setTarget(node2);
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(node1).addNode(node2).addEdge(edge1).build();

    // when
    countryResolver.deduceCountry(graph);

    // then
    Assertions.assertEquals("DEFAULT", countryResolver.getCountry());
  }

  @Test
  public void findValueInObjectsPartiallyWithoutRequiredCountryTag() {
    // given
    Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder().tags(new HashMap<>() {{
      put("addr:country", "PL");
    }}).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    edge1.setSource(node1);
    edge1.setTarget(node2);
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(node1).addNode(node2).addEdge(edge1).build();

    // when
    countryResolver.deduceCountry(graph);

    // then
    Assertions.assertEquals("PL", countryResolver.getCountry());
  }

  @Test
  public void findValueInObjectsWithRequiredCountryTag() {
    // given
    Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder().tags(new HashMap<>() {{
      put("addr:country", "UK");
    }}).build());
    Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder().tags(new HashMap<>() {{
      put("addr:country", "PL");
    }}).build());
    Node<JunctionData, WayData> node3 = new Node<>("3", JunctionData.builder().tags(new HashMap<>() {{
      put("addr:country", "PL");
    }}).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>()).build());
    edge1.setSource(node1);
    edge1.setTarget(node2);
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(node1).addNode(node2).addNode(node3).addEdge(edge1).build();

    // when
    countryResolver.deduceCountry(graph);

    // then
    Assertions.assertEquals("PL", countryResolver.getCountry());
  }

  @Test
  public void findValueInObjectsWithRequiredCountryTagInEdges() {
    // given
    Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder().tags(new HashMap<>()).build());
    Node<JunctionData, WayData> node3 = new Node<>("3", JunctionData.builder().tags(new HashMap<>()).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("addr:country", "UK");
    }}).build());
    Edge<JunctionData, WayData> edge2 = new Edge<>("2", WayData.builder().tags(new HashMap<>() {{
      put("addr:country", "UK");
    }}).build());
    Edge<JunctionData, WayData> edge3 = new Edge<>("3", WayData.builder().tags(new HashMap<>() {{
      put("addr:country", "PL");
    }}).build());
    edge1.setSource(node1);
    edge1.setTarget(node2);
    edge2.setSource(node2);
    edge2.setTarget(node3);
    edge3.setSource(node3);
    edge3.setTarget(node1);
    Graph<JunctionData, WayData> graph = new GraphBuilder<JunctionData, WayData>().addNode(node1)
        .addNode(node2)
        .addNode(node3)
        .addEdge(edge1)
        .addEdge(edge2)
        .addEdge(edge3)
        .build();

    // when
    countryResolver.deduceCountry(graph);

    // then
    Assertions.assertEquals("UK", countryResolver.getCountry());
  }
}
