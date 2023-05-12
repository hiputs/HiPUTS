package pl.edu.agh.hiputs.service.worker;

import java.util.HashMap;
import java.util.Map;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.exception.ModelValidationException;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadReadable;
import pl.edu.agh.hiputs.service.worker.usecase.ModelValidatorService;

@Service
public class ModelValidatorServiceImpl implements ModelValidatorService {

  @Override
  public void checkModel(boolean deadEnd, MapFragment mapFragment) throws ModelValidationException {
    Map<String, String> errors = new HashMap<>();

    checkMapFragmentInitialization(mapFragment, errors);
    checkInitializationOfRoads(mapFragment, errors);
    checkJunctions(mapFragment, errors, deadEnd);

  }

  private void checkJunctions(MapFragment mapFragment, Map<String, String> errors, boolean deadEnd)
      throws ModelValidationException {
    boolean passValidation = mapFragment.getKnownPatchReadable()
        .parallelStream()
        .flatMap(patch -> patch.getJunctionIds().stream().map(patch::getJunctionReadable))
        .allMatch(junction -> validateJunction(junction, deadEnd, errors));

    if (!passValidation) {
      throw new ModelValidationException(errors);
    }
  }

  private boolean validateJunction(JunctionReadable junction, boolean deadEnd, Map<String, String> errors) {
    if (junction.getJunctionId() == null || StringUtils.isBlank(junction.getJunctionId().getValue())) {
      errors.put("junctionId", "IS_NULL");
    }

    if (junction.streamIncomingRoadIds().count() + junction.streamOutgoingRoadIds().count() <= 0) {
      errors.put("roadsCount", "LOST_STATE");
    }

    if (deadEnd && (junction.streamIncomingRoadIds().findAny().isEmpty() || junction.streamOutgoingRoadIds()
        .findAny()
        .isEmpty())) {
      errors.put("roadsCount", "DEAD_END");
    }

    return errors.size() <= 0;
  }

  private void checkInitializationOfRoads(MapFragment mapFragment, Map<String, String> errors)
      throws ModelValidationException {

    boolean passValidation = mapFragment.getKnownPatchReadable()
        .parallelStream()
        .flatMap(patch -> patch.getRoadIds().stream().map(patch::getRoadReadable))
        .allMatch(road -> validateRoad(road, errors));

    if (!passValidation) {
      throw new ModelValidationException(errors);
    }
  }

  private boolean validateRoad(RoadReadable road, Map<String, String> errors) {
    if (road.getRoadId() == null || StringUtils.isBlank(road.getRoadId().getValue())) {
      errors.put("roadId", "IS_NULL");
    }

    if (road.getIncomingJunctionId() == null) {
      errors.put("incoming junction", "IS_NULL");
    }

    if (road.getOutgoingJunctionId() == null) {
      errors.put("outgoingJunction", "IS_NULL");
    }

    if (road.getLength() <= 0) {
      errors.put("road length", "TOO_SHORT ");
    }

    if (errors.size() > 0) {
      return false;
    }
    return true;
  }

  private void checkMapFragmentInitialization(MapFragment mapFragment, Map<String, String> errors)
      throws ModelValidationException {

    if (mapFragment.getKnownPatchReadable() == null) {
      errors.put("knownPatches", "IS_NULL");
    }

    if (mapFragment.getNeighbors() == null) {
      errors.put("neighbours", "IS_NULL");
    }

    if (mapFragment.getBorderPatches() == null) {
      errors.put("borderPatches", "IS_NULL");
    }

    if (mapFragment.getLocalJunctionIds() == null) {
      errors.put("localJunction", "IS_NULL");
    }

    if (mapFragment.getLocalRoadIds() == null) {
      errors.put("roads", "IS_NULL");
    }

    if (mapFragment.getShadowPatchesReadable() == null) {
      errors.put("patch2Actor", "IS_NULL");
    }

    if (errors.size() > 0) {
      throw new ModelValidationException(errors);
    }
  }
}
