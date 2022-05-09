package pl.edu.agh.utils;

import java.util.List;
import java.util.Map;

import java.util.Set;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.stream.Collectors;

import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;

@UtilityClass
public class DeterminingNeighborhoodUtil {

    /**
     * Function get all patch in system (after parsing map) and returns Patches with determining the neighborhood variable filled in
     *
     * @param allPatches - all patches in system
     */
    public static void execute(List<Patch> allPatches) {
        Map<JunctionId, PatchId> junction2Patch = new HashMap<>();
        allPatches.forEach(patch -> {
            patch.getJunctionIds().forEach(
                    junctionId -> junction2Patch.put(junctionId, patch.getPatchId())
            );
        });

        allPatches.forEach(patch -> {
            Set<PatchId> neighbourhoodPatchIds = patch.getLaneIds()
                    .parallelStream()
                    .map(patch::getLaneReadable)
                    .filter(lane -> patch.getJunctionReadable(lane.getOutgoingJunctionId()) == null ||
                            patch.getJunctionReadable(lane.getIncomingJunctionId()) == null)
                    .map(lane -> junction2Patch.get(lane.getOutgoingJunctionId()).equals(patch.getPatchId())
                            ? junction2Patch.get(lane.getIncomingJunctionId())
                            : junction2Patch.get(lane.getOutgoingJunctionId()))
                    .collect(Collectors.toSet());

            patch.getNeighboringPatches().addAll(neighbourhoodPatchIds);
        });
    }
}
