package pl.edu.agh.hiputs.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferMessage;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferNotificationMessage;
import pl.edu.agh.hiputs.communication.service.MessageSenderService;
import pl.edu.agh.hiputs.model.actor.MapFragment;
import pl.edu.agh.hiputs.model.car.Car;
import pl.edu.agh.hiputs.model.car.Route;
import pl.edu.agh.hiputs.model.car.RouteElement;
import pl.edu.agh.hiputs.model.car.RouteLocation;
import pl.edu.agh.hiputs.model.id.*;
import pl.edu.agh.hiputs.model.map.Lane;
import pl.edu.agh.hiputs.model.map.Patch;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PatchTransferServiceTest {
    @Autowired
    private PatchTransferServiceImpl patchTransferService;

    @MockBean
    private MapFragment mapFragment;

    @MockBean
    private MessageSenderService messageSenderService;

    @SneakyThrows
    @Test
    void shouldSendToNeighbour() {
        //given
        Patch patch = getSimplePatch();

        //when
        patchTransferService.sendPatch(new MapFragmentId("NEIGHBOUR"), patch);

        //then
        ArgumentCaptor<PatchTransferMessage> argumentCaptor = ArgumentCaptor.forClass(PatchTransferMessage.class);
        ArgumentCaptor<PatchTransferNotificationMessage> notificationArgumentCaptor = ArgumentCaptor.forClass(PatchTransferNotificationMessage.class);

        verify(messageSenderService, times(1))
                .send(eq(new MapFragmentId("NEIGHBOUR")), argumentCaptor.capture());

        verify(messageSenderService, times(1))
                .broadcast(notificationArgumentCaptor.capture());

        verify(mapFragment, times(1))
                .migrateMyPatchToNeighbour(eq(new PatchId("PATCH_ID")), eq(new MapFragmentId("NEIGHBOUR")));

        PatchTransferMessage patchTransferMessage = argumentCaptor.getValue();
        PatchTransferNotificationMessage notificationMessage = notificationArgumentCaptor.getValue();

        assertEquals(2, patchTransferMessage.getSLanes().size());
        assertEquals("PATCH_ID", notificationMessage.getTransferPatchId());
        assertEquals("NEIGHBOUR", notificationMessage.getReceiverId());
    }
    
    
    private Patch getSimplePatch() {
        Lane lane1 = Lane.builder().build();
        lane1.addIncomingCar(getCar("C1"));
        lane1.addIncomingCar(getCar("C2"));
        
        Lane lane2 = Lane.builder().build();
        lane2.addIncomingCar(getCar("C3"));
        lane2.addIncomingCar(getCar("C4"));
        
        return Patch.builder().lanes(Map.of(lane1.getId(), lane1, lane2.getId(), lane2)).build();
    }

    private Car getCar(String id) {
        List<RouteElement> routeElementList = List.of(
                new RouteElement(new JunctionId("zxc", JunctionType.BEND), new LaneId("vbn")),
                new RouteElement(new JunctionId("zxc1", JunctionType.BEND), new LaneId("vbn1"))
        );

        Route route = new Route(routeElementList);
        return Car.builder()
                .id(new CarId(id))
                .length(12)
                .speed(13)
                .maxSpeed(14)
                .laneId(new LaneId("abc"))
                .positionOnLane(0)
                .routeLocation(new RouteLocation(route))
                .build();
    }
}
