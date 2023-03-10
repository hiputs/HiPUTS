package pl.edu.agh.hiputs.service.worker;

import java.util.HashMap;
import java.util.Map;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.exception.ModelValidationException;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;
import pl.edu.agh.hiputs.service.worker.usecase.ModelValidatorService;

@Service
public class ModelValidatorServiceImpl implements ModelValidatorService {

  @Override
  public void checkModel(boolean deadEnd, MapFragment mapFragment) throws ModelValidationException {
    Map<String, String> errors = new HashMap<>();

    checkMapFragmentInitialization(mapFragment, errors);
    checkInitializationOfLanes(mapFragment, errors);
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

    if (junction.streamIncomingLaneIds().count() + junction.streamOutgoingLaneIds().count() <= 0) {
      errors.put("lanesCount", "LOST_STATE");
    }

    if (deadEnd && (junction.streamIncomingLaneIds().findAny().isEmpty() || junction.streamOutgoingLaneIds()
        .findAny()
        .isEmpty())) {
      errors.put("lanesCount", "DEAD_END");
    }

    return errors.size() <= 0;
  }

  private void checkInitializationOfLanes(MapFragment mapFragment, Map<String, String> errors)
      throws ModelValidationException {

    boolean passValidation = mapFragment.getKnownPatchReadable()
        .parallelStream()
        .flatMap(patch -> patch.getLaneIds().stream().map(patch::getLaneReadable))
        .allMatch(lane -> validateLane(lane, errors));

    if (!passValidation) {
      throw new ModelValidationException(errors);
    }
  }

  private boolean validateLane(LaneReadable lane, Map<String, String> errors) {
    if (lane.getLaneId() == null || StringUtils.isBlank(lane.getLaneId().getValue())) {
      errors.put("laneId", "IS_NULL");
    }

    if (lane.getIncomingJunctionId() == null) {
      errors.put("incoming junction", "IS_NULL");
    }

    if (lane.getOutgoingJunctionId() == null) {
      errors.put("outgoingJunction", "IS_NULL");
    }

    if (lane.getLength() <= 0) {
      errors.put("lane length", "TOO_SHORT ");
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

    if (mapFragment.getLocalLaneIds() == null) {
      errors.put("lanes", "IS_NULL");
    }

    if (mapFragment.getShadowPatchesReadable() == null) {
      errors.put("patch2Actor", "IS_NULL");
    }

    if (errors.size() > 0) {
      throw new ModelValidationException(errors);
    }
  }
}
