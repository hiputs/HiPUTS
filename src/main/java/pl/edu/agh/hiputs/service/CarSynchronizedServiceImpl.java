package pl.edu.agh.hiputs.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.CarTransferMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.serializable.SCar;
import pl.edu.agh.hiputs.communication.service.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.SubscriptionService;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.scheduler.task.CarMapperTask;
import pl.edu.agh.hiputs.scheduler.task.InjectIncomingCarsTask;
import pl.edu.agh.hiputs.service.usecase.CarSynchronizedService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarSynchronizedServiceImpl implements CarSynchronizedService, Subscriber {

  private final SubscriptionService subscriptionService;
  private final TaskExecutorService taskExecutorService;
  private final MessageSenderService messageSenderService;

  private final List<CarTransferMessage> incomingMessages = new ArrayList<>();
  private final List<CarTransferMessage> featureIncomingMessages = new ArrayList<>();

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.CarTransferMessage);
  }

  @Override
  public void sendCarsToNeighbours(TransferDataHandler mapFragment) {
    Map<MapFragmentId, List<SCar>> serializedCarMap = new HashMap<>();
    List<Runnable> tasks = new ArrayList<>();
    Map<MapFragmentId, Set<Patch>> borderPatches = mapFragment.getBorderPatches();

    borderPatches.forEach((mapFragmentId, patches) -> {
      List<SCar> toSendCars = serializedCarMap.computeIfAbsent(mapFragmentId, k -> new ArrayList<>());
      patches.forEach(patch -> tasks.add(new CarMapperTask(patch, toSendCars)));
    });

    taskExecutorService.executeBatch(tasks);
    sendMessages(serializedCarMap);
  }

  private void sendMessages(Map<MapFragmentId, List<SCar>> serializedCarMap) {
    for (Map.Entry<MapFragmentId, List<SCar>> entry : serializedCarMap.entrySet()) {
      CarTransferMessage carTransferMessage = new CarTransferMessage(entry.getValue());
      try {
        messageSenderService.send(entry.getKey(), carTransferMessage);
      } catch (IOException e) {
        log.error("Error sending message CarTransferMessage to: " + entry.getKey(), e);
      }
    }
  }

  @SneakyThrows
  @Override
  public void synchronizedGetIncomingCar(TransferDataHandler mapFragment) {
    int countOfNeighbours = mapFragment.getNeighbors().size();
    while (incomingMessages.size() < countOfNeighbours) {
      this.wait();
    }

    List<Runnable> injectIncomingCarTasks = incomingMessages.stream()
        .map(message -> new InjectIncomingCarsTask(message.getCars(), mapFragment))
        .collect(Collectors.toList());

    taskExecutorService.executeBatch(injectIncomingCarTasks);

    incomingMessages.clear();
    incomingMessages.addAll(featureIncomingMessages);
    featureIncomingMessages.clear();
  }

  @Override
  public void notify(Message message) {
    if (message.getMessageType() != MessagesTypeEnum.CarTransferMessage) {
      return;
    }

    CarTransferMessage carTransferMessage = (CarTransferMessage) message;
    if (incomingMessages.contains(carTransferMessage)) {
      featureIncomingMessages.add(carTransferMessage);
    } else {
      incomingMessages.add(carTransferMessage);
    }

    notifyAll();
  }
}
