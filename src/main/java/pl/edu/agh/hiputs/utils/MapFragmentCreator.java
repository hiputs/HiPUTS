package pl.edu.agh.hiputs.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment.MapFragmentBuilder;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class MapFragmentCreator {

  private final MapRepository mapRepository;

  public MapFragment fromMessage(ServerInitializationMessage initializationMessage, MapFragmentId mapFragmentId) {

    MapFragmentBuilder mapFragmentBuilder = MapFragment.builder(mapFragmentId);

    try {
      initializationMessage.getPatchIds()
          .stream()
          .map(PatchId::new)
          .forEach(id -> mapFragmentBuilder.addLocalPatch(mapRepository.getPatch(id)));

      initializationMessage.getWorkerInfo().forEach(worker -> {
        worker.getPatchIds()
            .stream()
            .map(PatchId::new)
            .forEach(id -> mapFragmentBuilder.addRemotePatch(new MapFragmentId(worker.getConnectionData().getId()),
                mapRepository.getPatch(id)));
      });
    } catch (Exception e){
      log.error("Error during build mapfragment", e);
    }

    return mapFragmentBuilder.build();
  }
}
