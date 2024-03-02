package pl.edu.agh.hiputs.model.car.driver.lights;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.model.car.driver.deciders.CarPrecedingEnvironment;
import pl.edu.agh.hiputs.model.car.driver.deciders.follow.Idm;
import pl.edu.agh.hiputs.model.car.driver.deciders.lights.RedGreenTrafficLightsDecider;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.Road;

@ExtendWith(MockitoExtension.class)
public class RedGreenTrafficLightsDeciderTest {
  private final RedGreenTrafficLightsDecider decider = new RedGreenTrafficLightsDecider(
      new Idm(1, 1, 1, 1, 1));
  @Mock
  private MapFragment mapFragment;

  @Test
  public void nullEnvironment() {
    // given

    // when

    // then
    Assertions.assertTrue(decider.tryToMakeDecision(null, null, mapFragment).isEmpty());
  }

  @Test
  public void emptyIncomingRoadId() {
    // given
    CarPrecedingEnvironment carEnvironment = new CarPrecedingEnvironment(Optional.empty(), Optional.empty(), 0);

    // when

    // then
    Assertions.assertTrue(decider.tryToMakeDecision(null, carEnvironment, mapFragment).isEmpty());
  }

  @Test
  public void emptyTrafficIndicator() {
    // given
    RoadId roadId = new RoadId("1");
    LaneId laneId = new LaneId("1");
    Road road = new Road(roadId, List.of(laneId), null, null, null, null, 0, Optional.empty());
    CarPrecedingEnvironment carEnvironment =
        new CarPrecedingEnvironment(0, Optional.empty(), Optional.empty(), Optional.of(roadId), Optional.of(laneId));

    // when
    Mockito.when(mapFragment.getRoadReadable(roadId)).thenReturn(road);

    // then
    Assertions.assertTrue(decider.tryToMakeDecision(null, carEnvironment, mapFragment).isEmpty());
  }

  // @Test
  // public void greenLightTrafficIndicator() {
  //   // given
  //   TrafficIndicator trafficIndicator = new TrafficIndicator();
  //   trafficIndicator.switchColor(LightColor.GREEN);
  //
  //   RoadId roadId = new RoadId("1");
  //   Road road = new Road(roadId, Collections.emptyList(), null, null,
  //       null, null, 0, Optional.of(trafficIndicator));
  //   CarPrecedingEnvironment carEnvironment = new CarPrecedingEnvironment(
  //       0, Optional.empty(), Optional.empty(), Optional.of(roadId));
  //
  //   // when
  //   Mockito.when(mapFragment.getRoadReadable(roadId)).thenReturn(road);
  //
  //   // then
  //   Assertions.assertTrue(decider.tryToMakeDecision(null, carEnvironment, mapFragment).isEmpty());
  // }
  //
  // @Test
  // public void typicalRedLightTrafficIndicator() {
  //   // given
  //   TrafficIndicator trafficIndicator = new TrafficIndicator();
  //   RoadId roadId = new RoadId("1");
  //   Road road = new Road(roadId, Collections.emptyList(), null, null,
  //       null, null, 0, Optional.of(trafficIndicator));
  //   CarPrecedingEnvironment carEnvironment = new CarPrecedingEnvironment(
  //       0, Optional.empty(), Optional.empty(),Optional.empty(), Optional.of(roadId));
  //   Car car = new Car(new CarId("2"), 1, 2, null, null, 0,
  //       null, 1, 1, null, Optional.empty());
  //
  //   // when
  //   Mockito.when(mapFragment.getRoadReadable(roadId)).thenReturn(road);
  //   Optional<JunctionDecision> junctionDecision = decider.tryToMakeDecision(car, carEnvironment, mapFragment);
  //
  //   // then
  //   Assertions.assertTrue(junctionDecision.isPresent());
  // }
}
