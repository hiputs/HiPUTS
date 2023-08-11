package pl.edu.agh.hiputs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.communication.model.messages.CarTransferMessage;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.CarEditable;
import pl.edu.agh.hiputs.model.car.Decision;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.scheduler.SchedulerService;
import pl.edu.agh.hiputs.service.worker.CarSynchronizationServiceImpl;

@ExtendWith(MockitoExtension.class)
public class IncomingCarsSetsSynchronizationServiceTest {

  @Mock
  private MapFragment mapFragment;

  @Mock
  private MessageSenderService messageSenderService;

  @Mock
  private WorkerSubscriptionService subscriptionService;

  private final SchedulerService taskExecutorService = new SchedulerService();

  @SneakyThrows
  @Test
  void shouldSendFromTwoPatchesToOneNeighbour() {
    //given
    when(mapFragment.pollOutgoingCars()).thenReturn(getOutgoingCars1());

    //when
    taskExecutorService.init();
    CarSynchronizationServiceImpl
        carSynchronizedService = new CarSynchronizationServiceImpl(subscriptionService, taskExecutorService, messageSenderService);
    carSynchronizedService.sendIncomingSetsOfCarsToNeighbours(mapFragment);

    //then
    ArgumentCaptor<CarTransferMessage> argumentCaptor = ArgumentCaptor.forClass(CarTransferMessage.class);

    verify(messageSenderService, times(1)).send(eq(new MapFragmentId("Actor1")), argumentCaptor.capture());

    assertEquals(8, argumentCaptor.getValue().getCars().size());
  }

  private Map<MapFragmentId, Set<CarEditable>> getOutgoingCars1() {
    return Map.of(new MapFragmentId("Actor1"), getCarsSet(Set.of("C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8")));
  }

  private Car getCar(String id) {
    List<RouteElement> routeElementList =
        List.of(new RouteElement(new JunctionId("zxc", JunctionType.BEND), new RoadId("vbn")),
            new RouteElement(new JunctionId("zxc1", JunctionType.BEND), new RoadId("vbn1")));

    return Car.builder()
        .carId(new CarId(id))
        .length(12)
        .speed(13)
        .maxSpeed(14)
        .roadId(new RoadId("abc"))
        .laneId(new LaneId("def"))
        .positionOnLane(0)
        .routeWithLocation(new RouteWithLocation(routeElementList, 0))
        .decision(Decision.builder().roadId(new RoadId("destination")).build())
        .build();
  }

  @SneakyThrows
  @Test
  void shouldSendFromTwoPatchesToTwoNeighbour() {
    //given
    when(mapFragment.pollOutgoingCars()).thenReturn(getOutgoingCars2());
    //when
    taskExecutorService.init();
    CarSynchronizationServiceImpl
        carSynchronizedService = new CarSynchronizationServiceImpl(subscriptionService, taskExecutorService, messageSenderService);
    carSynchronizedService.sendIncomingSetsOfCarsToNeighbours(mapFragment);

    //then
    ArgumentCaptor<CarTransferMessage> argumentCaptor = ArgumentCaptor.forClass(CarTransferMessage.class);
    ArgumentCaptor<CarTransferMessage> argumentCaptor2 = ArgumentCaptor.forClass(CarTransferMessage.class);

    verify(messageSenderService, times(1)).send(eq(new MapFragmentId("Actor1")), argumentCaptor.capture());

    verify(messageSenderService, times(1)).send(eq(new MapFragmentId("Actor2")), argumentCaptor2.capture());

    assertEquals(4, argumentCaptor.getValue().getCars().size());
    assertEquals(4, argumentCaptor2.getValue().getCars().size());
  }

  private Map<MapFragmentId, Set<CarEditable>> getOutgoingCars2() {
    return Map.of(new MapFragmentId("Actor1"), getCarsSet(Set.of("C1", "C2", "C3", "C4")),
        new MapFragmentId("Actor2"), getCarsSet(Set.of("C5", "C6", "C7", "C8")));
  }

  private Set<CarEditable> getCarsSet(Set<String> carsIds) {
    return carsIds.stream().map(this::getCar).collect(Collectors.toSet());
  }
}
