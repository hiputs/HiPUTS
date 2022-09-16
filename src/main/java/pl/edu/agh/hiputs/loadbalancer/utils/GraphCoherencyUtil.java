package pl.edu.agh.hiputs.loadbalancer.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;

@UtilityClass
public class GraphCoherencyUtil {

  /**
   * 1. Create GodPatch -> Reduce the graph to the form of a large vertex (all local patches connected with border
   * patches) and patches
   * adjacent to the neighbor
   *
   * 2. Make as visiting all patch connected with god patch and his child as visited. Use iterative algorithm.
   *
   * 3. Check, all border patches are marked as visited
   */
  public static boolean isCoherency(TransferDataHandler transferDataHandler, PatchId removedPatchId, MapFragmentId borderParticipant) {

    //1.
    Patch godPatch = createGodPatch(transferDataHandler, borderParticipant, removedPatchId);

    //2.
    Set<PatchId> visitedPatchId = new HashSet<>();
    Queue<PatchId> waitingForProcessed = new LinkedList<>(godPatch.getNeighboringPatches().stream().toList());
    Map<PatchId, Patch> neighbouringPatchesRepository = transferDataHandler.getBorderPatches()
        .get(borderParticipant)
        .stream()
        .collect(Collectors.toMap(Patch::getPatchId, Function.identity()));
    neighbouringPatchesRepository.remove(removedPatchId);

    while (!waitingForProcessed.isEmpty()) {
      PatchId patchId = waitingForProcessed.poll();

      if (visitedPatchId.contains(patchId)) {
        continue;
      }

     Patch patch = neighbouringPatchesRepository.get(patchId);

      if(patch == null){
        continue;
      }

      visitedPatchId.add(patch.getPatchId());

      waitingForProcessed.addAll(patch.getNeighboringPatches()
          .stream()
          .filter(p -> !visitedPatchId.contains(p))
          .filter(neighbouringPatchesRepository::containsKey)
          .toList());
    }

    //3.
    return visitedPatchId.size() == neighbouringPatchesRepository.size();
}

  private static Patch createGodPatch(TransferDataHandler transferDataHandler, MapFragmentId borderParticipant,
      PatchId removedPatchId) {

    Set<PatchId> godPatchNeighbours = transferDataHandler.getBorderPatches()
        .get(borderParticipant)
        .stream()
        .map(patch -> patch.streamJunctionsReadable()
            .map(j -> j.streamIncomingLaneIds().collect(Collectors.toSet()))
            .toList())
        .flatMap(List::stream)
        .flatMap(Set::stream)
        .distinct()
        .map(transferDataHandler::getPatchIdByLaneId)
        .distinct()
        .filter(transferDataHandler::isLocalPatch)
        .collect(Collectors.toSet());

    godPatchNeighbours.remove(removedPatchId);
    return Patch.builder().neighboringPatches(godPatchNeighbours).build();
  }
}
