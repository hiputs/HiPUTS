package pl.edu.agh.hiputs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferMessage;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferNotificationMessage;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.id.CarId;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.Lane;
import pl.edu.agh.hiputs.model.map.roadstructure.Road;
import pl.edu.agh.hiputs.service.worker.PatchTransferServiceImpl;

@SpringBootTest
@Disabled("TODO remove visualization during test")
public class PatchTransferServiceTest {

  @Autowired
  private PatchTransferServiceImpl patchTransferService;

  @MockBean
  private MapFragment mapFragment;

  @MockBean
  private MessageSenderService messageSenderService;

  @SneakyThrows
  @Disabled("future work")
  @Test
  void shouldSendToNeighbour() {
    //given
    Patch patch = getSimplePatch();

    //when
    // patchTransferService.sendPatch(new MapFragmentId("NEIGHBOUR"), patch);

    //then
    ArgumentCaptor<PatchTransferMessage> argumentCaptor = ArgumentCaptor.forClass(PatchTransferMessage.class);
    ArgumentCaptor<PatchTransferNotificationMessage> notificationArgumentCaptor =
        ArgumentCaptor.forClass(PatchTransferNotificationMessage.class);

    verify(messageSenderService, times(1)).send(eq(new MapFragmentId("NEIGHBOUR")), argumentCaptor.capture());

    verify(messageSenderService, times(1)).broadcast(notificationArgumentCaptor.capture());

    //        verify(mapFragment, times(1))
    //                .migrateMyPatchToNeighbour(eq(new PatchId("PATCH_ID")), eq(new MapFragmentId("NEIGHBOUR")));

    PatchTransferNotificationMessage notificationMessage = notificationArgumentCaptor.getValue();

    assertEquals("PATCH_ID", notificationMessage.getTransferPatchId());
    assertEquals("NEIGHBOUR", notificationMessage.getReceiverId());
  }

  private Patch getSimplePatch() {
    LaneId laneId1 = LaneId.random();
    RoadId roadId1 = RoadId.random();
    Lane lane1 = Lane.builder().laneId(laneId1).roadId(roadId1).build();
    Road road1 = Road.builder().roadId(roadId1).lanes(Collections.singletonList(laneId1)).build();
    lane1.addIncomingCar(getCar("C1"));
    lane1.addIncomingCar(getCar("C2"));

    LaneId laneId2 = LaneId.random();
    RoadId roadId2 = RoadId.random();
    Lane lane2 = Lane.builder().laneId(laneId2).roadId(roadId2).build();
    Road road2 = Road.builder().roadId(roadId2).lanes(Collections.singletonList(laneId2)).build();
    lane2.addIncomingCar(getCar("C3"));
    lane2.addIncomingCar(getCar("C4"));

    return Patch.builder()
        .roads(Map.of(road1.getRoadId(), road1, road2.getRoadId(), road2))
        .lanes(Map.of(lane1.getLaneId(), lane1, lane2.getLaneId(), lane2))
        .build();
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
        .roadId(new RoadId("abc")).positionOnLane(0)
        .routeWithLocation(new RouteWithLocation(routeElementList, 0))
        .build();
  }
}
