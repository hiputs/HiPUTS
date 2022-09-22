package pl.edu.agh.hiputs.service.worker.usecase;

import java.util.List;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;

public interface MapRepository {

  void readMapAndBuildModel() throws InterruptedException;

  List<Patch> getPatches(List<PatchId> patchIds);

  Patch getPatch(PatchId id);

  boolean isReady();

  List<Patch> getAllPatches();
}
