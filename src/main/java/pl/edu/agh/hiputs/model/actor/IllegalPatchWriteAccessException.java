package pl.edu.agh.hiputs.model.actor;

import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;

public class IllegalPatchWriteAccessException extends RuntimeException {

    public IllegalPatchWriteAccessException(PatchId patchId, LaneId laneId) {
        super(String.format("Lane with id \"%s\" in Patch \"%s\" cannot be modified from this map fragment", laneId.toString(), patchId.toString()));
    }

}
