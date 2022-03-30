package pl.edu.agh.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.exception.ModelValidationException;
import pl.edu.agh.model.actor.MapFragment;
import pl.edu.agh.model.id.JunctionId;
import pl.edu.agh.model.id.JunctionType;
import pl.edu.agh.model.id.LaneId;
import pl.edu.agh.model.map.Junction;
import pl.edu.agh.model.map.Lane;
import pl.edu.agh.model.map.Patch;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ModelValidatorServiceTest {

    @InjectMocks
    private ModelValidatorServiceImpl modelValidatorService;

    @Mock
    private MapFragment mapFragment;

    @Test
    void shouldFailCheckMapFragment() {
        when(mapFragment.getLocalPatches()).thenReturn(null);
        when(mapFragment.getRemotePatches()).thenReturn(null);
        when(mapFragment.getBorderPatches()).thenReturn(null);
        when(mapFragment.getLane2Patch()).thenReturn(null);
        when(mapFragment.getNeighbours()).thenReturn(null);
        when(mapFragment.getPatch2Actor()).thenReturn(null);

        ModelValidationException exception = null;

        try{
            modelValidatorService.checkModel();
        } catch (ModelValidationException e){
            exception = e;
        }

        assertTrue(exception != null);
        assertEquals(6, exception.getErrors().size());
    }

    @Test
    void shouldFailLanesCheck(){
        when(mapFragment.getLocalPatches()).thenReturn(getMockPatchWithLainFail());

        ModelValidationException exception = null;

        try{
            modelValidatorService.checkModel();
        } catch (ModelValidationException e){
            exception = e;
        }

        assertTrue(exception != null);
        assertEquals(5, exception.getErrors().size());
        assertEquals(exception.toString(), "ModelValidationException(errors={laneId=NOT_NULL, lane2Patch=NOT_NULL, outgoingJunction=NOT_NULL, incoming junction=NOT_NULL, lane length=TO_SHORT})");
    }

    @Test
    void shouldFailJunctionCheck(){
        when(mapFragment.getLocalPatches()).thenReturn(getMockPatchWithJunctionFail());

        ModelValidationException exception = null;

        try{
            modelValidatorService.checkModel();
        } catch (ModelValidationException e){
            exception = e;
        }

        assertTrue(exception != null);
        assertEquals(2, exception.getErrors().size());
    }

    private Collection<Patch> getMockPatchWithJunctionFail() {
        Junction junction = new Junction(new JunctionId("", JunctionType.BEND));

        Patch patch = new Patch();
        patch.setJunctions(Map.of(junction.getId(), junction));
        patch.setLanes(Map.of());

        return List.of(patch);
    }

    private Collection<Patch> getMockPatchWithLainFail() {
        Lane lane = new Lane(new LaneId(""));
        lane.setIncomingCars(null);
        lane.setOutgoingJunction(null);
        lane.setIncomingJunction(null);
        lane.setLength(0);

        Patch patch = new Patch();
        patch.setLanes(Map.of(lane.getId(), lane));

        return List.of(patch);
    }
}
