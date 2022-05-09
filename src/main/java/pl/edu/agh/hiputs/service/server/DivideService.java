package pl.edu.agh.hiputs.service.server;

import java.util.List;
import java.util.Map;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;

public interface DivideService {

  Map<String, List<PatchId>> divide(List<Patch> patches);


}
