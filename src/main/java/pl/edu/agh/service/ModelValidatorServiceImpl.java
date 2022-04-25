package pl.edu.agh.service;

import lombok.RequiredArgsConstructor;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.exception.ModelValidationException;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneReadable;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ModelValidatorServiceImpl implements ModelValidatorService {

    private final MapFragment mapFragment;

    @Override
    public void checkModel() throws ModelValidationException {
        Map<String, String> errors = new HashMap<>();

        checkMapFragmentInitialization(errors);
        checkInitializationOfLanes(errors);
        checkJunction(errors);

    }

    private void checkJunction(Map<String, String> errors) throws ModelValidationException {
        boolean passValidation = mapFragment.getKnownPatchReadable()
                .parallelStream()
                .flatMap(patch -> patch.getJunctionIds()
                    .stream()
                    .map(patch::getJunctionReadable))
                .allMatch(junction -> validateJunction(junction, errors));

        if (!passValidation) {
            throw new ModelValidationException(errors);
        }
    }

    private boolean validateJunction(JunctionReadable junction, Map<String, String> errors) {
        if (junction.getJunctionId() == null || StringUtils.isBlank(junction.getJunctionId().getValue())) {
            errors.put("junctionId", "NOT_NULL");
        }

        if (junction.streamIncomingLaneIds().count() + junction.streamOutgoingLaneIds().count() <= 0) {
            errors.put("lanesCount", "LOST_STATE");
        }

        if (errors.size() > 0) {
            return false;
        }

        return true;
    }

    private void checkInitializationOfLanes(Map<String, String> errors) throws ModelValidationException {

        boolean passValidation = mapFragment.getKnownPatchReadable()
                .parallelStream()
                .flatMap(patch -> patch.getLaneIds()
                    .stream()
                    .map(patch::getLaneReadable))
                .allMatch(lane -> validateLane(lane, errors));

        if (!passValidation) {
            throw new ModelValidationException(errors);
        }
    }

    private boolean validateLane(LaneReadable lane, Map<String, String> errors) {
        if (lane.getLaneId() == null || StringUtils.isBlank(lane.getLaneId().getValue())) {
            errors.put("laneId", "NOT_NULL");
        }

        if (lane.getIncomingJunctionId() == null) {
            errors.put("incoming junction", "NOT_NULL");
        }

        if (lane.getOutgoingJunctionId() == null) {
            errors.put("outgoingJunction", "NOT_NULL");
        }

        if (lane.getLength() <= 0) {
            errors.put("lane length", "TO_SHORT");
        }

        if (errors.size() > 0) {
            return false;
        }
        return true;
    }

    private void checkMapFragmentInitialization(Map<String, String> errors) throws ModelValidationException {

        if (mapFragment.getKnownPatchReadable() == null) {
            errors.put("localPatches", "NOT_NULL");
        }

        if (mapFragment.getNeighbors() == null) {
            errors.put("neighbours", "NOT_NULL");
        }

        if (mapFragment.getBorderPatches() == null) {
            errors.put("borderPatches", "NOT_NULL");
        }

        if (mapFragment.getLocalJunctionIds() == null) {
            errors.put("localJunction", "NOT_NULL");
        }

        if (mapFragment.getLocalLaneIds() == null) {
            errors.put("lanes", "NOT_NULL");
        }

        if (mapFragment.getShadowPatchesReadable() == null) {
            errors.put("patch2Actor", "NOT_NULL");
        }

        if (errors.size() > 0) {
            throw new ModelValidationException(errors);
        }
    }
}
