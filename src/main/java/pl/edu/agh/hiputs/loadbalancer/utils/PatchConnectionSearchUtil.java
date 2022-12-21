package pl.edu.agh.hiputs.loadbalancer.utils;

import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;

@UtilityClass
public class PatchConnectionSearchUtil {

  public static List<PatchId> findNeighbouringPatches(PatchId patchId, TransferDataHandler transferDataHandler){
    return transferDataHandler.getPatchById(patchId).getNeighboringPatches()
        .stream()
        .filter(transferDataHandler::isLocalPatch)
        .toList();
  }

  public static List<PatchId> findShadowPatchesNeighbouringOnlyWithPatch(PatchId patchId, TransferDataHandler transferDataHandler){
    Patch patch = transferDataHandler.getPatchById(patchId);

    return patch.getNeighboringPatches()
        .stream()
        .filter(id -> !transferDataHandler.isLocalPatch(id))
        .map(transferDataHandler::getPatchById)
        .filter(shadowPatch ->
            shadowPatch != null && shadowPatch.getNeighboringPatches()
                .stream()
                .noneMatch(transferDataHandler::isLocalPatch))
        .map(Patch::getPatchId)
        .toList();
  }

  public static boolean isEndPatch(Patch patch, MapFragmentId neighbourId, TransferDataHandler mapFragment) {
    return patch.getNeighboringPatches()
        .stream()
        .map(mapFragment::getMapFragmentIdByPatchId)
        .filter(Objects::nonNull)
        .noneMatch(neighbourId::equals);
  }
}
