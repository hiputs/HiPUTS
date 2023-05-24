package pl.edu.agh.hiputs.task;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.partition.model.lights.LightColor;
import pl.edu.agh.hiputs.partition.model.lights.control.StandardSignalsControlCenter;
import pl.edu.agh.hiputs.partition.model.lights.group.MultipleTIsGreenColorGroup;
import pl.edu.agh.hiputs.partition.model.lights.indicator.TrafficIndicator;
import pl.edu.agh.hiputs.service.worker.strategies.RedGreenOnlyTrafficLightsStrategy;
import pl.edu.agh.hiputs.tasks.JunctionLightsUpdateStageTask;

@ExtendWith(MockitoExtension.class)
public class JunctionLightsUpdateStageTaskTest {

  @Test
  public void updateWhenPresent() {
    // given
    MapFragment mapFragment = Mockito.mock(MapFragment.class, Mockito.RETURNS_DEEP_STUBS);
    StandardSignalsControlCenter signalsControlCenter = new StandardSignalsControlCenter(1);
    TrafficIndicator indicator1 = new TrafficIndicator();
    TrafficIndicator indicator2 = new TrafficIndicator();
    MultipleTIsGreenColorGroup group1 = new MultipleTIsGreenColorGroup(List.of(indicator1));
    MultipleTIsGreenColorGroup group2 = new MultipleTIsGreenColorGroup(List.of(indicator2));
    signalsControlCenter.getGreenColorGroups().addAll(List.of(group1, group2));

    // when
    Mockito.when(mapFragment.getJunctionReadable(Mockito.any()).getSignalsControlCenter())
        .thenReturn(Optional.of(signalsControlCenter));
    JunctionLightsUpdateStageTask task = new JunctionLightsUpdateStageTask(mapFragment,
        Mockito.mock(JunctionId.class), new RedGreenOnlyTrafficLightsStrategy());

    task.run(); // init
    LightColor color1Before = indicator1.getCurrentColor();
    LightColor color2Before = indicator2.getCurrentColor();
    task.run();
    task.run();

    // then
    Assertions.assertEquals(LightColor.RED, color1Before);
    Assertions.assertEquals(LightColor.GREEN, color2Before);
    Assertions.assertEquals(LightColor.GREEN, indicator1.getCurrentColor());
    Assertions.assertEquals(LightColor.RED, indicator2.getCurrentColor());
  }
}
