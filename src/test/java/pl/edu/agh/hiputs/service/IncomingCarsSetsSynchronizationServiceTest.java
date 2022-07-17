package pl.edu.agh.hiputs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.communication.model.messages.CarTransferMessage;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.scheduler.SchedulerService;
import pl.edu.agh.hiputs.service.worker.IncomingCarsSetsSynchronizationServiceImpl;

@ExtendWith(MockitoExtension.class)
public class IncomingCarsSetsSynchronizationServiceTest {

  @Mock
  private MapFragment mapFragment;

  @Mock
  private MessageSenderService messageSenderService;

  @Mock
  private SubscriptionService subscriptionService;

  private final SchedulerService taskExecutorService = new SchedulerService();

  @SneakyThrows
  @Test
  void shouldSendFromTwoPatchesToOneNeighbour() {
    //given
    when(mapFragment.getBorderPatches()).thenReturn(getBorderPatches());

    //when
    taskExecutorService.init();
    IncomingCarsSetsSynchronizationServiceImpl
        carSynchronizedService = new IncomingCarsSetsSynchronizationServiceImpl(subscriptionService, taskExecutorService, messageSenderService);
    carSynchronizedService.sendIncomingSetsOfCarsToNeighbours(mapFragment);

    //then
    ArgumentCaptor<CarTransferMessage> argumentCaptor = ArgumentCaptor.forClass(CarTransferMessage.class);

    verify(messageSenderService, times(1)).send(eq(new MapFragmentId("Actor1")), argumentCaptor.capture());

    assertEquals(8, argumentCaptor.getValue().getCars().size());
  }

  private Map<MapFragmentId, Set<Patch>> getBorderPatches() {
    return Map.of(new MapFragmentId("Actor1"), Set.of(getSimplePatch(), getSimplePatch2()));
  }

  private Patch getSimplePatch() {
    Lane lane1 = Lane.builder()
        .laneId(new LaneId("lane1"))
        .build();
    lane1.addIncomingCar(getCar("C1"));
    lane1.addIncomingCar(getCar("C2"));

    Lane lane2 = Lane.builder()
        .laneId(new LaneId("lane2"))
        .build();
    lane2.addIncomingCar(getCar("C3"));
    lane2.addIncomingCar(getCar("C4"));

    return Patch.builder()
        .lanes(Map.of(
            lane1.getLaneId(), lane1,
            lane2.getLaneId(), lane2))
        .build();
  }

  private Car getCar(String id) {
    List<RouteElement> routeElementList =
        List.of(new RouteElement(new JunctionId("zxc", JunctionType.BEND), new LaneId("vbn")),
            new RouteElement(new JunctionId("zxc1", JunctionType.BEND), new LaneId("vbn1")));

    return Car.builder()
        .carId(new CarId(id))
        .length(12)
        .speed(13)
        .maxSpeed(14)
        .laneId(new LaneId("abc"))
        .positionOnLane(0)
        .routeWithLocation(new RouteWithLocation(routeElementList, 0))
        .build();
  }

  private Patch getSimplePatch2() {
    Lane lane1 = Lane.builder()
        .laneId(new LaneId("Lane3"))
        .build();
    lane1.addIncomingCar(getCar("C5"));
    lane1.addIncomingCar(getCar("C6"));

    Lane lane2 = Lane.builder()
        .laneId(new LaneId("Lane4"))
        .build();
    lane2.addIncomingCar(getCar("C7"));
    lane2.addIncomingCar(getCar("C8"));

    return Patch.builder().lanes(Map.of(lane1.getLaneId(), lane1, lane2.getLaneId(), lane2)).build();
  }

  @SneakyThrows
  @Test
  void shouldSendFromTwoPatchesToTwoNeighbour() {
    //given
    when(mapFragment.getBorderPatches()).thenReturn(getBorderPatches2());
    //when
    taskExecutorService.init();
    IncomingCarsSetsSynchronizationServiceImpl
        carSynchronizedService = new IncomingCarsSetsSynchronizationServiceImpl(subscriptionService, taskExecutorService, messageSenderService);
    carSynchronizedService.sendIncomingSetsOfCarsToNeighbours(mapFragment);

    //then
    ArgumentCaptor<CarTransferMessage> argumentCaptor = ArgumentCaptor.forClass(CarTransferMessage.class);
    ArgumentCaptor<CarTransferMessage> argumentCaptor2 = ArgumentCaptor.forClass(CarTransferMessage.class);

    verify(messageSenderService, times(1)).send(eq(new MapFragmentId("Actor1")), argumentCaptor.capture());

    verify(messageSenderService, times(1)).send(eq(new MapFragmentId("Actor2")), argumentCaptor2.capture());

    assertEquals(4, argumentCaptor.getValue().getCars().size());
    assertEquals(4, argumentCaptor2.getValue().getCars().size());
  }

  private Map<MapFragmentId, Set<Patch>> getBorderPatches2() {
    return Map.of(new MapFragmentId("Actor1"), Set.of(getSimplePatch()),
        new MapFragmentId("Actor2"), Set.of( getSimplePatch2())
        );
  }
}
