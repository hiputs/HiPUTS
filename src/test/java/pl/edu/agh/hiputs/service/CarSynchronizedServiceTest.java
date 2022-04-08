package pl.edu.agh.hiputs.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pl.edu.agh.hiputs.communication.model.messages.CarTransferMessage;
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
import static org.mockito.Mockito.*;

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
        when(mapFragment.getBorderPatches())
                .thenReturn(getAdjacentPatches());

        when(mapFragment.getPatch2Actor())
                .thenReturn(getPatch2Actor());

        //when
        carSynchronizedService.sendCarsToNeighbours();

        //then
        ArgumentCaptor<CarTransferMessage> argumentCaptor = ArgumentCaptor.forClass(CarTransferMessage.class);

        verify(messageSenderService, times(1))
                .send(eq(new MapFragmentId("Actor1")), argumentCaptor.capture());

        assertEquals(8, argumentCaptor.getValue().getCars().size());
    }

    @SneakyThrows
    @Test
    void shouldSendFromTwoPatchesToTwoNeighbour() {
        //given
        when(mapFragment.getBorderPatches())
                .thenReturn(getAdjacentPatches());

        when(mapFragment.getPatch2Actor())
                .thenReturn(getAdjacentPatchesFor2Neighbour());

        //when
        carSynchronizedService.sendCarsToNeighbours();

        //then
        ArgumentCaptor<CarTransferMessage> argumentCaptor = ArgumentCaptor.forClass(CarTransferMessage.class);
        ArgumentCaptor<CarTransferMessage> argumentCaptor2 = ArgumentCaptor.forClass(CarTransferMessage.class);

        verify(messageSenderService, times(1))
                .send(eq(new MapFragmentId("Actor1")), argumentCaptor.capture());

        verify(messageSenderService, times(1))
                .send(eq(new MapFragmentId("Actor2")), argumentCaptor2.capture());

        assertEquals(4, argumentCaptor.getValue().getCars().size());
        assertEquals(4, argumentCaptor2.getValue().getCars().size());
    }

    private Map<PatchId, MapFragmentId> getAdjacentPatchesFor2Neighbour() {
        return Map.of(
                new PatchId("Patch1"), new MapFragmentId("Actor1"),
                new PatchId("Patch2"), new MapFragmentId("Actor2")
        );
    }

    private Map<PatchId, MapFragmentId> getPatch2Actor() {
        return Map.of(
                new PatchId("Patch1"), new MapFragmentId("Actor1"),
                new PatchId("Patch2"), new MapFragmentId("Actor1")
        );

    }

    private Map<PatchId, Patch> getAdjacentPatches() {
        return Map.of(
                new PatchId("Patch1"), getSimplePatch(),
                new PatchId("Patch2"), getSimplePatch2()
        );
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

    private Patch getSimplePatch2() {
        Lane lane1 = Lane.builder().build();
        lane1.addIncomingCar(getCar("C5"));
        lane1.addIncomingCar(getCar("C6"));
        
        Lane lane2 = Lane.builder().build();
        lane2.addIncomingCar(getCar("C7"));
        lane2.addIncomingCar(getCar("C8"));
    
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
