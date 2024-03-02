package pl.edu.agh.hiputs.service.worker.strategies;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.partition.model.lights.LightColor;
import pl.edu.agh.hiputs.partition.model.lights.control.StandardSignalsControlCenter;
import pl.edu.agh.hiputs.partition.model.lights.group.MultipleTIsGreenColorGroup;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicator;

@ExtendWith(MockitoExtension.class)
public class RedGreenOnlyTrafficLightsStrategyTest {
  private final RedGreenOnlyTrafficLightsStrategy strategy = new RedGreenOnlyTrafficLightsStrategy();

  @Test
  public void incrementDurationStepWhenConditionNotMet() {
    // given
    StandardSignalsControlCenter signalsControlCenter = new StandardSignalsControlCenter(2);

    // when
    strategy.execute(signalsControlCenter);
    int step1 = signalsControlCenter.getCurrentStep();
    strategy.execute(signalsControlCenter);
    int step2 = signalsControlCenter.getCurrentStep();
    strategy.execute(signalsControlCenter);

    // then
    Assertions.assertEquals(0, step1);
    Assertions.assertEquals(1, step2);
    Assertions.assertEquals(2, signalsControlCenter.getCurrentStep());
  }

  @Test
  public void changeColorWhenConditionMet() {
    // given
    StandardSignalsControlCenter signalsControlCenter = new StandardSignalsControlCenter(1);
    TrafficIndicator indicator1 = new TrafficIndicator();
    TrafficIndicator indicator2 = new TrafficIndicator();
    MultipleTIsGreenColorGroup group1 = new MultipleTIsGreenColorGroup(List.of(indicator1));
    MultipleTIsGreenColorGroup group2 = new MultipleTIsGreenColorGroup(List.of(indicator2));
    signalsControlCenter.getGreenColorGroups().addAll(List.of(group1, group2));

    // when
    strategy.execute(signalsControlCenter);
    int step1 = signalsControlCenter.getCurrentStep();
    LightColor color1Before = indicator1.getCurrentColor();
    LightColor color2Before = indicator2.getCurrentColor();
    strategy.execute(signalsControlCenter);
    int step2 = signalsControlCenter.getCurrentStep();
    LightColor color1After1 = indicator1.getCurrentColor();
    LightColor color2After1 = indicator2.getCurrentColor();
    strategy.execute(signalsControlCenter);

    // then
    Assertions.assertEquals(0, step1);
    Assertions.assertEquals(LightColor.RED, color1Before);
    Assertions.assertEquals(LightColor.GREEN, color2Before);
    Assertions.assertEquals(1, step2);
    Assertions.assertEquals(LightColor.RED, color1After1);
    Assertions.assertEquals(LightColor.GREEN, color2After1);
    Assertions.assertEquals(0, signalsControlCenter.getCurrentStep());
    Assertions.assertEquals(LightColor.GREEN, indicator1.getCurrentColor());
    Assertions.assertEquals(LightColor.RED, indicator2.getCurrentColor());
  }
}
