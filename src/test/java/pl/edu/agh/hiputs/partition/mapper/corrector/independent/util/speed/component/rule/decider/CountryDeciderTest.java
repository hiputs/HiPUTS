package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.decider;

import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.resolver.StandardCountryResolver;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;

@ExtendWith(MockitoExtension.class)
public class CountryDeciderTest {

  @Mock
  private StandardCountryResolver standardCountryResolver;
  @InjectMocks
  private CountryDecider countryDecider;

  private SpeedResultHandler speedResultHandler;

  @BeforeEach
  public void init() {
    speedResultHandler = new SpeedResultHandler();
  }

  @Test
  public void setDefaultWhenGivenEdgeAndNodesAreEmpty() {
    // given
    Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder().tags(Collections.emptyMap()).build());
    Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder().tags(Collections.emptyMap()).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(Collections.emptyMap()).build());
    edge1.setSource(node1);
    edge1.setTarget(node2);
    speedResultHandler.setEdge(edge1);

    // when
    Mockito.when(standardCountryResolver.getCountry()).thenReturn("UK");
    countryDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals("UK", speedResultHandler.getCountry());
  }

  @Test
  public void setFoundValueDeducedFromEdge() {
    // given
    Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder().tags(Collections.emptyMap()).build());
    Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder().tags(Collections.emptyMap()).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("addr:country", "PL");
    }}).build());
    edge1.setSource(node1);
    edge1.setTarget(node2);
    speedResultHandler.setEdge(edge1);

    // when
    Mockito.when(standardCountryResolver.getCountry()).thenReturn("UK");
    countryDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals("PL", speedResultHandler.getCountry());
  }

  @Test
  public void setFoundValueDeducedFromNodes() {
    // given
    Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder().tags(new HashMap<>() {{
      put("addr:country", "DE");
    }}).build());
    Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder().tags(new HashMap<>() {{
      put("addr:country", "DE");
    }}).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder().tags(new HashMap<>() {{
      put("addr:country", "PL");
    }}).build());
    edge1.setSource(node1);
    edge1.setTarget(node2);
    speedResultHandler.setEdge(edge1);

    // when
    Mockito.when(standardCountryResolver.getCountry()).thenReturn("UK");
    countryDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals("DE", speedResultHandler.getCountry());
  }
}
