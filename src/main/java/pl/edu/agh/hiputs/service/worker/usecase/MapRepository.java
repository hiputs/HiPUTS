package pl.edu.agh.hiputs.service.worker.usecase;

import java.util.List;
import java.util.Map;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;

public interface MapRepository {

  void readMapAndBuildModel() throws InterruptedException;

  List<Patch> getPatches(List<PatchId> patchIds);

  Patch getPatch(Patch patch);

}
