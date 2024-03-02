package pl.edu.agh.hiputs.partition.osm.speed.rule.decider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.SpeedResultHandler;
import pl.edu.agh.hiputs.partition.osm.speed.rule.handler.TypeOfRoad;
import pl.edu.agh.hiputs.partition.osm.speed.table.repository.SpeedLimitRecord;
import pl.edu.agh.hiputs.partition.osm.speed.table.repository.SpeedLimitsRepositoryImpl;

@ExtendWith(MockitoExtension.class)
public class ResultDeciderTest {
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SpeedLimitsRepositoryImpl speedLimitsRepository;
  @InjectMocks
  private ResultDecider resultDecider;

  private SpeedResultHandler speedResultHandler;
  private SpeedLimitRecord speedLimitRecord;

  @BeforeEach
  public void init() {
    speedResultHandler = new SpeedResultHandler();
    speedLimitRecord = new SpeedLimitRecord();
    speedLimitRecord.setHighway("90");
    speedLimitRecord.setRural("70");
    speedLimitRecord.setUrban("50");
  }

  @Test
  public void setHighwaySpeedLimit() {
    // given
    speedResultHandler.setTypeOfRoad(TypeOfRoad.Highway);

    // when
    Mockito.when(speedLimitsRepository.getMapOfCountriesWithSpeedLimits().get(Mockito.any()))
        .thenReturn(speedLimitRecord);
    resultDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals("90", speedResultHandler.getResultSpeed());
  }


  @Test
  public void setRuralSpeedLimit() {
    // given
    speedResultHandler.setTypeOfRoad(TypeOfRoad.Rural);

    // when
    Mockito.when(speedLimitsRepository.getMapOfCountriesWithSpeedLimits().get(Mockito.any()))
        .thenReturn(speedLimitRecord);
    resultDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals("70", speedResultHandler.getResultSpeed());
  }

  @Test
  public void setUrbanSpeedLimit() {
    // given
    speedResultHandler.setTypeOfRoad(TypeOfRoad.Urban);

    // when
    Mockito.when(speedLimitsRepository.getMapOfCountriesWithSpeedLimits().get(Mockito.any()))
        .thenReturn(speedLimitRecord);
    resultDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertEquals("50", speedResultHandler.getResultSpeed());
  }

  @Test
  public void notSetSpeedLimit() {
    // given
    speedResultHandler.setTypeOfRoad(TypeOfRoad.NotClassified);

    // when
    Mockito.when(speedLimitsRepository.getMapOfCountriesWithSpeedLimits().get(Mockito.any()))
        .thenReturn(speedLimitRecord);
    resultDecider.decideAboutValue(speedResultHandler);

    // then
    Assertions.assertNull(speedResultHandler.getResultSpeed());
  }
}
