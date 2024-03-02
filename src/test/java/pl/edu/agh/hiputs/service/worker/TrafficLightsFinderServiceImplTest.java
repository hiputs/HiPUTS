package pl.edu.agh.hiputs.service.worker;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.partition.model.lights.control.StandardSignalsControlCenter;

@ExtendWith(MockitoExtension.class)
public class TrafficLightsFinderServiceImplTest {
  private final TrafficLightsFinderServiceImpl finder = new TrafficLightsFinderServiceImpl();

  @Test
  public void notGetOnEmptyList() {
    // given
    MapFragment mapFragment = Mockito.mock(MapFragment.class, Mockito.RETURNS_DEEP_STUBS);

    // when
    Mockito.when(mapFragment.getLocalJunctionIds()).thenReturn(Collections.emptySet());

    // then
    Assertions.assertEquals(0, finder.getJunctionIds(mapFragment).size());
  }

  @Test
  public void notGetOnListWithNonSignalControlledJunctions() {
    // given
    MapFragment mapFragment = Mockito.mock(MapFragment.class, Mockito.RETURNS_DEEP_STUBS);
    JunctionId junctionId = new JunctionId("1", JunctionType.CROSSROAD);

    // when
    Mockito.when(mapFragment.getLocalJunctionIds()).thenReturn(Set.of(junctionId));
    Mockito.when(mapFragment.getJunctionReadable(junctionId).getSignalsControlCenter()).thenReturn(Optional.empty());

    // then
    Assertions.assertEquals(0, finder.getJunctionIds(mapFragment).size());
  }

  @Test
  public void getCorrectJunctionWithSignalControlCenters() {
    // given
    MapFragment mapFragment = Mockito.mock(MapFragment.class, Mockito.RETURNS_DEEP_STUBS);
    JunctionId junctionId = new JunctionId("1", JunctionType.CROSSROAD);
    StandardSignalsControlCenter signalsControlCenter = new StandardSignalsControlCenter(1);

    // when
    Mockito.when(mapFragment.getLocalJunctionIds()).thenReturn(Set.of(junctionId));
    Mockito.when(mapFragment.getJunctionReadable(junctionId).getSignalsControlCenter())
        .thenReturn(Optional.of(signalsControlCenter));

    // then
    Assertions.assertEquals(1, finder.getJunctionIds(mapFragment).size());
  }
}
