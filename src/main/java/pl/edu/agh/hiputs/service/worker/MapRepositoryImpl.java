package pl.edu.agh.hiputs.service.worker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Repository
@RequiredArgsConstructor
public class MapRepositoryImpl implements MapRepository, Subscriber {

  private final Map<PatchId, Patch> patches = new HashMap<>();

  private final ConfigurationService configurationService;
  private final SubscriptionService subscriptionService;
  private boolean mapReadyToRead = false;

  @PostConstruct
  void init(){
    subscriptionService.subscribe(this, MessagesTypeEnum.MapReadyToRead);
  }

  @Override
  public void readMapAndBuildModel() throws InterruptedException {
      if(configurationService.getConfiguration().isParsedMap()){
        waitForMapReadyToReadMessage();
      }

      if(configurationService.getConfiguration().isServerOnThisMachine()){
        // toDo build repository from server collection
      }

      //ToDo code to read map from csv and build patches
  }

  private void waitForMapReadyToReadMessage() throws InterruptedException {
    while (!mapReadyToRead){
      wait();
    }
  }

  @Override
  public List<Patch> getPatches(List<PatchId> patchIds) {
    return null;
  }

  @Override
  public Patch getPatch(Patch patch) {
    return null;
  }

  @Override
  public void notify(Message message) {
    if(message.getMessageType() == MessagesTypeEnum.MapReadyToRead){
      mapReadyToRead = true;
      notifyAll();
    }
  }
}
