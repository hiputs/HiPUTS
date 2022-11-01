package pl.edu.agh.hiputs.loadbalancer.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;

@RequiredArgsConstructor
@Builder
@Getter
@AllArgsConstructor
public class PatchBalancingInfo {
    private final PatchId patchId;
    private int countOfVehicle;

    private List<PatchId> newBorderPatchesAfterTransfer;
    private int countCarsInNewBorderPatches;

    private List<PatchId> shadowPatchesToRemoveAfterTransfer;
    private int countCarsInRemovedShadowPatches;
}
