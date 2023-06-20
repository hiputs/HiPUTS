package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.decider.CountryDecider;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.decider.ResultDecider;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.decider.TypeOfRoadDecider;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.resolver.StandardCountryResolver;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitRecord;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitsRepository;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitsRepositoryImpl;
import pl.edu.agh.hiputs.partition.model.JunctionData;
import pl.edu.agh.hiputs.partition.model.WayData;
import pl.edu.agh.hiputs.partition.model.graph.Edge;
import pl.edu.agh.hiputs.partition.model.graph.Node;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@ExtendWith(MockitoExtension.class)
public class StandardRuleEngineTest {
  private final SpeedLimitsRepository speedLimitsRepository =
      Mockito.mock(SpeedLimitsRepositoryImpl.class, Mockito.RETURNS_DEEP_STUBS);

  private final StandardRuleEngine standardRuleEngine = new StandardRuleEngine(List.of(
      new TypeOfRoadDecider(),
      new CountryDecider(new StandardCountryResolver()),
      new ResultDecider(speedLimitsRepository, Mockito.mock(ModelConfigurationService.class))
  ));
  private SpeedLimitRecord speedLimitRecord;

  @BeforeEach
  public void init() {
    speedLimitRecord = new SpeedLimitRecord();
    speedLimitRecord.setCountryId("PL");
    speedLimitRecord.setHighway("140");
    speedLimitRecord.setRural("90");
    speedLimitRecord.setUrban("50");
  }

  @Test
  public void happyPath() {
    // given
    Node<JunctionData, WayData> node1 = new Node<>("1", JunctionData.builder()
        .tags(new HashMap<>(){{put("addr:country", "PL");}}).build());
    Node<JunctionData, WayData> node2 = new Node<>("2", JunctionData.builder()
        .tags(new HashMap<>(){{put("addr:country", "PL");}}).build());
    Edge<JunctionData, WayData> edge1 = new Edge<>("1", WayData.builder()
        .tags(new HashMap<>(){{put("highway", "primary");}}).build());
    edge1.setSource(node1);
    edge1.setTarget(node2);

    // when
    Mockito.when(speedLimitsRepository.getMapOfCountriesWithSpeedLimits().get("PL")).thenReturn(speedLimitRecord);

    // then
    Assertions.assertEquals(90, standardRuleEngine.processWay(edge1));
  }
}
