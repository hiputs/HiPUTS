package pl.edu.agh.hiputs.service.worker;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Repository;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.MapReadyToReadMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.loadbalancer.utils.GraphCoherencyUtil;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.partition.mapper.Internal2SimulationModelMapper;
import pl.edu.agh.hiputs.partition.model.PatchConnectionData;
import pl.edu.agh.hiputs.partition.model.PatchData;
import pl.edu.agh.hiputs.partition.model.graph.Graph;
import pl.edu.agh.hiputs.partition.persistance.PatchesGraphReader;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepositoryServerHandler;

@Repository
@RequiredArgsConstructor
public class MapRepositoryImpl implements MapRepository, Subscriber, MapRepositoryServerHandler {

  private final Map<PatchId, Patch> patches = new HashMap<>();

  private final ConfigurationService configurationService;
  private final SubscriptionService subscriptionService;

  private final PatchesGraphReader patchesGraphReader;
  private final Internal2SimulationModelMapper internal2SimulationModelMapper;
  private Path mapPackagePath;
  private boolean mapReadyToRead = false;
  private boolean mapReadyToUse = false;

  @Setter
  private Graph<PatchData, PatchConnectionData> patchesGraph;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.MapReadyToRead);
  }

  @Override
  public void readMapAndBuildModel() throws InterruptedException {
    if (configurationService.getConfiguration().isReadFromOsmDirectly()) {
      waitForMapReadyToReadMessage();
    } else {
      mapPackagePath = Path.of(configurationService.getConfiguration().getMapPath());
    }

    // if (!configurationService.getConfiguration().isServerOnThisMachine()) {
      this.patchesGraph = patchesGraphReader.readGraphWithPatches(mapPackagePath);
    // }

    patches.putAll(internal2SimulationModelMapper.mapToSimulationModel(patchesGraph));
    mapReadyToUse = true;
  }

  private synchronized void waitForMapReadyToReadMessage() throws InterruptedException {
    while (!mapReadyToRead) {
      wait();
    }
  }

  @Override
  public List<Patch> getPatches(List<PatchId> patchIds) {
    if (!mapReadyToUse) {
      throw new RuntimeException("Map is not ready to use");
    }
    return patchIds.stream().map(patches::get).toList();
  }

  @Override
  public Patch getPatch(PatchId id) {
    if (!mapReadyToUse) {
      throw new RuntimeException("Map is not ready to use");
    }
    return patches.get(id);
  }

  @Override
  public boolean isReady() {
    return mapReadyToUse;
  }

  @Override
  public List<Patch> getAllPatches() {
    return patches.values().stream().toList();
  }

  @Override
  public Map<PatchId, Patch> getPatchesMap() {
    return patches;
  }

  @Override
  public PatchId getPatchIdByLaneId(LaneId laneId) {
    return patches.values()
        .parallelStream()
        .filter(p -> p.getLaneIds().contains(laneId))
        .findFirst()
        .get()
        .getPatchId();
  }

  @Override
  public synchronized void notify(Message message) {
    if (message.getMessageType() == MessagesTypeEnum.MapReadyToRead) {
      mapPackagePath = Path.of(((MapReadyToReadMessage) message).getMapPackagePath());
      mapReadyToRead = true;
      notifyAll();
    }
  }
}
