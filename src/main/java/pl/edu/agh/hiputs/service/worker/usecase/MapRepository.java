package pl.edu.agh.hiputs.service.worker.usecase;

import java.util.List;
import java.util.Map;
import pl.edu.agh.hiputs.model.id.JunctionId;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;

public interface MapRepository {

  void readMapAndBuildModel() throws InterruptedException;

  List<Patch> getPatches(List<PatchId> patchIds);

  Patch getPatch(PatchId id);

  boolean isReady();

  List<Patch> getAllPatches();

  Map<PatchId, Patch> getPatchesMap();

  // only for debug
  PatchId getPatchIdByRoadId(RoadId roadId);

  JunctionReadable getJunctionReadable(JunctionId junctionId);
}
