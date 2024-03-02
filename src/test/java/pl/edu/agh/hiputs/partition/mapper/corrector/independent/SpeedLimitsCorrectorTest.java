package pl.edu.agh.hiputs.partition.mapper.corrector.independent;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.StandardRuleEngine;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.resolver.StandardCountryResolver;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.model.graph.Graph.GraphBuilder;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@ExtendWith(MockitoExtension.class)
public class SpeedLimitsCorrectorTest {

  private final StandardCountryResolver countryResolver = Mockito.mock(StandardCountryResolver.class);
  private final StandardRuleEngine ruleEngine = Mockito.mock(StandardRuleEngine.class);
  private final SpeedLimitsCorrector corrector = new SpeedLimitsCorrector(countryResolver, ruleEngine);

  @Test
  public void processEdgeWithKeyExisting() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(Map.of("maxspeed", "70")).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();

    // then
    corrector.correct(graph);
    Assertions.assertEquals(70, edge1.getData().getMaxSpeed());
    Mockito.verify(ruleEngine, Mockito.times(0)).processWay(Mockito.any());
  }

  @Test
  public void processEdgeWithoutKeyExisting() {
    // given
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(Map.of()).build());
    Node<JunctionData, WayData> nodeA = new Node<>("A", JunctionData.builder().build());
    Node<JunctionData, WayData> nodeB = new Node<>("B", JunctionData.builder().build());

    // when
    edge1.setSource(nodeA);
    edge1.setTarget(nodeB);
    Graph<JunctionData, WayData> graph =
        new GraphBuilder<JunctionData, WayData>().addNode(nodeA).addNode(nodeB).addEdge(edge1).build();
    Mockito.when(ruleEngine.processWay(Mockito.any())).thenReturn(50);

    // then
    corrector.correct(graph);
    Assertions.assertEquals(50, edge1.getData().getMaxSpeed());
    Mockito.verify(ruleEngine, Mockito.times(1)).processWay(Mockito.any());
  }
}
