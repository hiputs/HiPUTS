package pl.edu.agh.hiputs.service.worker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.CarTransferMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedCar;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.scheduler.task.CarMapperTask;
import pl.edu.agh.hiputs.scheduler.task.InjectIncomingCarsTask;
import pl.edu.agh.hiputs.service.worker.usecase.CarSynchronizationService;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarSynchronizationServiceImpl implements CarSynchronizationService, Subscriber {

  private final SubscriptionService subscriptionService;
  private final TaskExecutorService taskExecutorService;
  private final MessageSenderService messageSenderService;
  private final List<CarTransferMessage> incomingMessages = new ArrayList<>();
  private final List<CarTransferMessage> futureIncomingMessages = new ArrayList<>();

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.CarTransferMessage);
  }

  @Override
  public void sendIncomingSetsOfCarsToNeighbours(TransferDataHandler mapFragment) {
    Map<MapFragmentId, List<SerializedCar>> serializedCarMap = mapFragment.pollOutgoingCars()
        .entrySet()
        .parallelStream()
        .collect(Collectors.toMap(
            Entry::getKey,
            e -> e.getValue()
                .parallelStream()
                .map(SerializedCar::new)
                .collect(Collectors.toList())
        ));

    sendMessages(serializedCarMap);
  }

  @Override
  public List<SerializedCar> getSerializedCarByPatch(TransferDataHandler transferDataHandler, PatchId patchId) {
    Patch patch = transferDataHandler.getPatchById(patchId);

    return patch.getLaneIds()
        .parallelStream()
        .map(patch::getLaneEditable)
        .flatMap(LaneEditable::pollIncomingCars)
        .map(SerializedCar::new)
        .toList();
  }

  private void sendMessages(Map<MapFragmentId, List<SerializedCar>> serializedCarMap) {
    for (Map.Entry<MapFragmentId, List<SerializedCar>> entry : serializedCarMap.entrySet()) {
      CarTransferMessage carTransferMessage = new CarTransferMessage(entry.getValue());
      try {
        messageSenderService.send(entry.getKey(), carTransferMessage);
      } catch (IOException e) {
        log.error("Error sending message CarTransferMessage to: " + entry.getKey(), e);
      }
    }
  }

  @Override
  public synchronized void synchronizedGetIncomingSetsOfCars(TransferDataHandler mapFragment) {
    int countOfNeighbours = mapFragment.getNeighbors().size();
    while (incomingMessages.size() < countOfNeighbours) {
      try {
        this.wait();
      } catch (InterruptedException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

    List<Runnable> injectIncomingCarTasks = incomingMessages.stream()
        .map(message -> new InjectIncomingCarsTask(message.getCars(), mapFragment))
        .collect(Collectors.toList());

    taskExecutorService.executeBatch(injectIncomingCarTasks);

    incomingMessages.clear();
    incomingMessages.addAll(futureIncomingMessages);
    futureIncomingMessages.clear();
  }

  @Override
  public synchronized void notify(Message message) {
    if (message.getMessageType() != MessagesTypeEnum.CarTransferMessage) {
      return;
    }

    CarTransferMessage carTransferMessage = (CarTransferMessage) message;
    if (incomingMessages.contains(carTransferMessage)) {
      futureIncomingMessages.add(carTransferMessage);
    } else {
      incomingMessages.add(carTransferMessage);
    }

    notifyAll();
  }
}
