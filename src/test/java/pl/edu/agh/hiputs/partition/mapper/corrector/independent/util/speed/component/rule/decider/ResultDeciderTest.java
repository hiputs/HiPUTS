package pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.decider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.rule.handler.TypeOfRoad;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitRecord;
import pl.edu.agh.hiputs.partition.mapper.corrector.independent.util.speed.component.table.repository.SpeedLimitsRepositoryImpl;
import pl.edu.agh.hiputs.service.ModelConfigurationService;

@ExtendWith(MockitoExtension.class)
public class ResultDeciderTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ModelConfigurationService modelConfigService;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SpeedLimitsRepositoryImpl speedLimitsRepository;
  @InjectMocks
  private ResultDecider resultDecider;

  private SpeedResultHandler speedResultHandler;
  private SpeedLimitRecord speedLimitRecord;

  @BeforeEach
  public void init() {
    speedResultHandler = new SpeedResultHandler();
    speedResultHandler.setCountry("VV");
    speedLimitRecord = new SpeedLimitRecord();
    speedLimitRecord.setCountryId("VV");
    speedLimitRecord.setHighway("90");
    speedLimitRecord.setRural("70");
    speedLimitRecord.setUrban("50");
  }

  @Test
  public void setHighwaySpeedLimit() {
    // given
    speedResultHandler.setTypeOfRoad(TypeOfRoad.Highway);

    // when
    Mockito.when(speedLimitsRepository.getMapOfCountriesWithSpeedLimits().get("VV")).thenReturn(speedLimitRecord);
    resultDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(90, speedResultHandler.getResultSpeed());
  }

  @Test
  public void setRuralSpeedLimit() {
    // given
    speedResultHandler.setTypeOfRoad(TypeOfRoad.Rural);

    // when
    Mockito.when(speedLimitsRepository.getMapOfCountriesWithSpeedLimits().get("VV")).thenReturn(speedLimitRecord);
    resultDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(70, speedResultHandler.getResultSpeed());
  }

  @Test
  public void setUrbanSpeedLimit() {
    // given
    speedResultHandler.setTypeOfRoad(TypeOfRoad.Urban);

    // when
    Mockito.when(speedLimitsRepository.getMapOfCountriesWithSpeedLimits().get("VV")).thenReturn(speedLimitRecord);
    resultDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(50, speedResultHandler.getResultSpeed());
  }

  @Test
  public void notSetSpeedLimit() {
    // given
    speedResultHandler.setTypeOfRoad(TypeOfRoad.NotClassified);

    // when
    Mockito.when(modelConfigService.getModelConfig().getDefaultMaxSpeed()).thenReturn(30);
    Mockito.when(speedLimitsRepository.getMapOfCountriesWithSpeedLimits().get("VV")).thenReturn(speedLimitRecord);
    resultDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals(30, speedResultHandler.getResultSpeed());
  }
}

