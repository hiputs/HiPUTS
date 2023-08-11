package pl.edu.agh.hiputs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static pl.edu.agh.hiputs.example.ExampleMapFragmentProvider.getSimpleMap2;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.agh.hiputs.exception.ModelValidationException;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.JunctionType;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.patch.PatchReader;
import pl.edu.agh.hiputs.model.map.roadstructure.Junction;
import pl.edu.agh.hiputs.model.map.roadstructure.Road;
import pl.edu.agh.hiputs.service.worker.ModelValidatorServiceImpl;

@Disabled
@ExtendWith(MockitoExtension.class)
public class ModelValidatorServiceTest {

  @InjectMocks
  private ModelValidatorServiceImpl modelValidatorService;

  @Mock
  private MapFragment mapFragment;

  @Test
  void shouldFailCheckMapFragment() {
    when(mapFragment.getKnownPatchReadable()).thenReturn(null);
    when(mapFragment.getShadowPatchesReadable()).thenReturn(null);
    when(mapFragment.getBorderPatches()).thenReturn(null);
    when(mapFragment.getNeighbors()).thenReturn(null);
    when(mapFragment.getLocalRoadIds()).thenReturn(null);
    when(mapFragment.getLocalJunctionIds()).thenReturn(null);

    ModelValidationException exception = null;

    try {
      modelValidatorService.checkModel(false, mapFragment);
    } catch (ModelValidationException e) {
      exception = e;
    }

    assertTrue(exception != null);
    assertEquals(6, exception.getErrors().size());
  }

  @Test
  void shouldFailLanesCheck() {
    when(mapFragment.getKnownPatchReadable()).thenReturn(getMockPatchWithLainFail());

    ModelValidationException exception = null;

    try {
      modelValidatorService.checkModel(false, mapFragment);
    } catch (ModelValidationException e) {
      exception = e;
    }

    assertTrue(exception != null);
    assertEquals(3, exception.getErrors().size());
    assertEquals(exception.toString(),
        "ModelValidationException(errors={outgoingJunction=IS_NULL, incoming junction=IS_NULL, lane length=TOO_SHORT "
            + "})");
  }

  @Test
  void shouldFailJunctionCheck() {
    when(mapFragment.getKnownPatchReadable()).thenReturn(getMockPatchWithJunctionFail());

    ModelValidationException exception = null;

    try {
      modelValidatorService.checkModel(false, mapFragment);
    } catch (ModelValidationException e) {
      exception = e;
    }

    assertTrue(exception != null);
    assertEquals(2, exception.getErrors().size());
  }

  @Test
  void shouldValidateRealModel() {
    ModelValidationException exception = null;

    try {
      modelValidatorService.checkModel(false, getSimpleMap2());
    } catch (ModelValidationException e) {
      exception = e;
    }

    assertTrue(exception == null);
  }

  private Set<PatchReader> getMockPatchWithJunctionFail() {
    Junction junction = new Junction(new JunctionId("", JunctionType.BEND), 1.d, 1.d, Set.of(), Set.of(), List.of());

    Patch patch = new Patch(new PatchId("PATCH_ID"), Map.of(junction.getJunctionId(), junction), Map.of(), Map.of(), Set.of());

    return Set.of(patch);
  }

  private Set<PatchReader> getMockPatchWithLainFail() {
    Road road = new Road(new RoadId("ROAD_ID"), null, null, null, null, null,0);

    Patch patch = new Patch(new PatchId("PATCH_ID"), Map.of(), Map.of(road.getRoadId(), road), Map.of(), Set.of());

    return Set.of(patch);
  }
}
