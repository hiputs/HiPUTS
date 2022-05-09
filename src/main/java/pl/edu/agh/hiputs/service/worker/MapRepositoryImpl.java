package pl.edu.agh.hiputs.service.worker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Repository
@RequiredArgsConstructor
public class MapRepositoryImpl implements MapRepository {

  private final Map<PatchId, Patch> patches = new HashMap<>();

  @Override
  public void readMapAndBuildModel() {

  }

  @Override
  public List<Patch> getPatches(List<PatchId> patchIds) {
    return null;
  }

  @Override
  public Patch getPatch(Patch patch) {
    return null;
  }
}
