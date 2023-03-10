package pl.edu.agh.hiputs.service.worker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.scheduler.task.InjectIncomingCarsTask;
import pl.edu.agh.hiputs.service.worker.usecase.CarSynchronizationService;
import pl.edu.agh.hiputs.utils.DebugUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarSynchronizationServiceImpl implements CarSynchronizationService, Subscriber {

  private final WorkerSubscriptionService subscriptionService;
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
    int readedMessage = 0;
    while (incomingMessages.size() < countOfNeighbours && readedMessage < countOfNeighbours) {
      try {
        this.wait(1000);
        readedMessage += applyMessages(mapFragment, readedMessage);
      } catch (InterruptedException e) {
        log.error(e.getMessage());
        throw new RuntimeException(e);
      }
    }

    applyMessages(mapFragment, readedMessage);
    incomingMessages.addAll(futureIncomingMessages);
    futureIncomingMessages.clear();
  }

  private int applyMessages(TransferDataHandler mapFragment, int start) {
    List<Runnable> injectIncomingCarTasks = incomingMessages.subList(start, incomingMessages.size())
        .stream()
        .map(message -> new InjectIncomingCarsTask(message.getCars(), mapFragment))
        .collect(Collectors.toList());

    taskExecutorService.executeBatch(injectIncomingCarTasks);
    return injectIncomingCarTasks.size();
  }

  private String getWeaitingByMessage() {
    try {
      final Set<MapFragmentId> mapFragmentIds = incomingMessages.stream()
          .map(m -> DebugUtils.getMapFragment().getPatchIdByLaneId(new LaneId(m.getCars().get(0).getLaneId())))
          .map(patchId -> DebugUtils.getMapFragment().getMapFragmentIdByPatchId(patchId))
          .collect(Collectors.toSet());

      return DebugUtils.getMapFragment().getNeighbors()
          .stream()
          .filter(mapFragmentId -> !mapFragmentIds.contains(mapFragmentId))
          .map(mapFragmentId -> mapFragmentId.getId() + "{ " + DebugUtils.getMapFragment().getBorderPatches().get(mapFragmentId).stream().map(p -> p.getPatchId().getValue()).collect(
              Collectors.joining(", ")) + "}")
          .collect(Collectors.joining(", "));
    } catch (Exception e){

    }
    return "---";
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
