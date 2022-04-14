package pl.edu.agh.hiputs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pl.edu.agh.hiputs.communication.model.messages.CarTransferMessage;
import pl.edu.agh.hiputs.communication.service.MessageSenderService;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;

@SpringBootTest
public class CarSynchronizedServiceTest {

  @Autowired
  private CarSynchronizedServiceImpl carSynchronizedService;

  @MockBean
  private MapFragment mapFragment;

  @MockBean
  private MessageSenderService messageSenderService;

  @SneakyThrows
  @Test
  void shouldSendFromTwoPatchesToOneNeighbour() {
    //given
    when(mapFragment.getBorderPatches()).thenReturn(getAdjacentPatches());

    when(mapFragment.getPatch2Actor()).thenReturn(getPatch2Actor());

    //when
    carSynchronizedService.sendCarsToNeighbours();

    //then
    ArgumentCaptor<CarTransferMessage> argumentCaptor = ArgumentCaptor.forClass(CarTransferMessage.class);

    verify(messageSenderService, times(1)).send(eq(new MapFragmentId("Actor1")), argumentCaptor.capture());

    assertEquals(8, argumentCaptor.getValue().getCars().size());
  }

  private Map<PatchId, MapFragmentId> getPatch2Actor() {
    return Map.of(new PatchId("Patch1"), new MapFragmentId("Actor1"), new PatchId("Patch2"),
        new MapFragmentId("Actor1"));

  }

  private Map<PatchId, Patch> getAdjacentPatches() {
    return Map.of(new PatchId("Patch1"), getSimplePatch(), new PatchId("Patch2"), getSimplePatch2());
  }

  private Patch getSimplePatch() {
    Lane lane1 = Lane.builder().build();
    lane1.addIncomingCar(getCar("C1"));
    lane1.addIncomingCar(getCar("C2"));

    Lane lane2 = Lane.builder().build();
    lane2.addIncomingCar(getCar("C3"));
    lane2.addIncomingCar(getCar("C4"));

    return Patch.builder().lanes(Map.of(lane1.getLaneId(), lane1, lane2.getLaneId(), lane2)).build();
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
    Lane lane1 = Lane.builder().build();
    lane1.addIncomingCar(getCar("C5"));
    lane1.addIncomingCar(getCar("C6"));

    Lane lane2 = Lane.builder().build();
    lane2.addIncomingCar(getCar("C7"));
    lane2.addIncomingCar(getCar("C8"));

    return Patch.builder().lanes(Map.of(lane1.getLaneId(), lane1, lane2.getLaneId(), lane2)).build();
  }

  @SneakyThrows
  @Test
  void shouldSendFromTwoPatchesToTwoNeighbour() {
    //given
    when(mapFragment.getBorderPatches()).thenReturn(getAdjacentPatches());

    when(mapFragment.getPatch2Actor()).thenReturn(getAdjacentPatchesFor2Neighbour());

    //when
    carSynchronizedService.sendCarsToNeighbours();

    //then
    ArgumentCaptor<CarTransferMessage> argumentCaptor = ArgumentCaptor.forClass(CarTransferMessage.class);
    ArgumentCaptor<CarTransferMessage> argumentCaptor2 = ArgumentCaptor.forClass(CarTransferMessage.class);

    verify(messageSenderService, times(1)).send(eq(new MapFragmentId("Actor1")), argumentCaptor.capture());

    verify(messageSenderService, times(1)).send(eq(new MapFragmentId("Actor2")), argumentCaptor2.capture());

    assertEquals(4, argumentCaptor.getValue().getCars().size());
    assertEquals(4, argumentCaptor2.getValue().getCars().size());
  }

  private Map<PatchId, MapFragmentId> getAdjacentPatchesFor2Neighbour() {
    return Map.of(new PatchId("Patch1"), new MapFragmentId("Actor1"), new PatchId("Patch2"),
        new MapFragmentId("Actor2"));
  }
}
