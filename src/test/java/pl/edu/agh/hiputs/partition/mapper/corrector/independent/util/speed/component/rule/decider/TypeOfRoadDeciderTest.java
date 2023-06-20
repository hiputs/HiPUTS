package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.decider;

import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler.TypeOfRoad;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;

@ExtendWith(MockitoExtension.class)
public class TypeOfRoadDeciderTest {
  @InjectMocks
  private TypeOfRoadDecider typeOfRoadDecider;

  private SpeedResultHandler speedResultHandler;

  @BeforeEach
  public void init() {
    speedResultHandler = new SpeedResultHandler();
  }

  @Test
  public void notSettingAndThrow() {
    // given
    Edge<JunctionData, WayData> edge = new Edge<>("1", WayData.builder().tags(Collections.emptyMap()).build());
    speedResultHandler.setEdge(edge);

    // when

    // then
    Assertions.assertThrows(IllegalArgumentException.class, () -> typeOfRoadDecider.decideAboutValue(speedResultHandler));
  }

  @Test
  public void settingHighwayTypeByLabel() {
    // given
    Edge<JunctionData, WayData> edge = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "motorway");}}).build());
    speedResultHandler.setEdge(edge);

    // when
    typeOfRoadDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(TypeOfRoad.Highway, speedResultHandler.getTypeOfRoad());
  }

  @Test
  public void settingUrbanTypeByNameAndRural() {
    // given
    Edge<JunctionData, WayData> edge = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary"); put("name", "Zwierzyniecka");}}).build());
    speedResultHandler.setEdge(edge);

    // when
    typeOfRoadDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(TypeOfRoad.Urban, speedResultHandler.getTypeOfRoad());
  }

  @Test
  public void settingUrbanTypeByLabel() {
    // given
    Edge<JunctionData, WayData> edge = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "living_street");}}).build());
    speedResultHandler.setEdge(edge);

    // when
    typeOfRoadDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(TypeOfRoad.Urban, speedResultHandler.getTypeOfRoad());
  }

  @Test
  public void settingRuralTypeByLabel() {
    // given
    Edge<JunctionData, WayData> edge = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "secondary");}}).build());
    speedResultHandler.setEdge(edge);

    // when
    typeOfRoadDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(TypeOfRoad.Rural, speedResultHandler.getTypeOfRoad());
  }

  @Test
  public void settingNotClassifiedTypeByLabel() {
    // given
    Edge<JunctionData, WayData> edge = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "path");}}).build());
    speedResultHandler.setEdge(edge);

    // when
    typeOfRoadDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(TypeOfRoad.NotClassified, speedResultHandler.getTypeOfRoad());
  }
}

