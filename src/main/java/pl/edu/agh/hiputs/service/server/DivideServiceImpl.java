package pl.edu.agh.hiputs.service.server;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;

@Service
@RequiredArgsConstructor
public class DivideServiceImpl implements DivideService{

  @Override
  public Map<String, List<PatchId>> divide(List<Patch> patches) {
    return null;
  }
}
