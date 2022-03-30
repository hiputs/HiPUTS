package pl.edu.agh.service;

import lombok.RequiredArgsConstructor;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.exception.ModelValidationException;
import pl.edu.agh.model.actor.MapFragment;
import pl.edu.agh.model.map.Junction;
import pl.edu.agh.model.map.Lane;

import java.util.Collection;
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
        boolean passValidation = mapFragment.getLocalPatches()
                .parallelStream()
                .map(patch -> patch.getJunctions().values())
                .flatMap(Collection::stream)
                .allMatch(junction -> validateJunction(junction, errors));

        if (!passValidation) {
            throw new ModelValidationException(errors);
        }
    }

    private boolean validateJunction(Junction junction, Map<String, String> errors) {
        if (junction.getId() == null || StringUtils.isBlank(junction.getId().getValue())) {
            errors.put("junctionId", "NOT_NULL");
        }

        if (junction.getLanesCount() <= 0) {
            errors.put("lanesCount", "LOST_STATE");
        }

        if (errors.size() > 0) {
            return false;
        }

        return true;
    }

    private void checkInitializationOfLanes(Map<String, String> errors) throws ModelValidationException {

        boolean passValidation = mapFragment.getLocalPatches()
                .parallelStream()
                .map(patch -> patch.getLanes().values())
                .flatMap(Collection::stream)
                .allMatch(lane -> validateLane(lane, errors));

        if (!passValidation) {
            throw new ModelValidationException(errors);
        }
    }

    private boolean validateLane(Lane lane, Map<String, String> errors) {
        if (lane.getId() == null || StringUtils.isBlank(lane.getId().getValue())) {
            errors.put("laneId", "NOT_NULL");
        }

        if (lane.getIncomingJunction() == null) {
            errors.put("incoming junction", "NOT_NULL");
        }

        if (lane.getOutgoingJunction() == null) {
            errors.put("outgoingJunction", "NOT_NULL");
        }

        if (lane.getIncomingCars() == null) {
            errors.put("lane2Patch", "NOT_NULL");
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

        if (mapFragment.getLocalPatches() == null) {
            errors.put("localPatches", "NOT_NULL");
        }

        if (mapFragment.getRemotePatches() == null) {
            errors.put("remotePatches", "NOT_NULL");
        }

        if (mapFragment.getBorderPatches() == null) {
            errors.put("borderPatches", "NOT_NULL");
        }

        if (mapFragment.getLane2Patch() == null) {
            errors.put("lane2Patch", "NOT_NULL");
        }

        if (mapFragment.getNeighbours() == null) {
            errors.put("neighbours", "NOT_NULL");
        }

        if (mapFragment.getPatch2Actor() == null) {
            errors.put("patch2Actor", "NOT_NULL");
        }

        if (errors.size() > 0) {
            throw new ModelValidationException(errors);
        }
    }
}
