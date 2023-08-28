package pl.edu.agh.hiputs.service.worker;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.CarTransferMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedCar;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedLane;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.WorkerSubscriptionService;
import pl.edu.agh.hiputs.model.id.RoadId;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.model.id.PatchId;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.LaneEditable;
import pl.edu.agh.hiputs.model.map.roadstructure.RoadEditable;
import pl.edu.agh.hiputs.scheduler.TaskExecutorService;
import pl.edu.agh.hiputs.scheduler.task.InjectIncomingCarsTask;
import pl.edu.agh.hiputs.scheduler.task.LaneSerializationTask;
import pl.edu.agh.hiputs.service.worker.usecase.CarSynchronizationService;
import pl.edu.agh.hiputs.utils.DebugUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarSynchronizationServiceImpl implements CarSynchronizationService, Subscriber {

  private final WorkerSubscriptionService subscriptionService;
  private final TaskExecutorService taskExecutorService;
  private final MessageSenderService messageSenderService;
  private final BlockingQueue<CarTransferMessage> incomingMessages = new LinkedBlockingQueue<>();
  private final BlockingQueue<CarTransferMessage> futureIncomingMessages = new LinkedBlockingQueue<>();

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.CarTransferMessage);
  }

  @Override
  public int sendIncomingSetsOfCarsToNeighbours(TransferDataHandler mapFragment) {
    Map<MapFragmentId, List<SerializedCar>> serializedCarMap = mapFragment.pollOutgoingCars()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey,
            e -> e.getValue().stream().map(SerializedCar::new).collect(Collectors.toList())));

    sendMessages(serializedCarMap);
    return serializedCarMap.values().stream().map(List::size).reduce(0, Integer::sum);
  }

  @Override
  public List<SerializedLane> getSerializedCarByPatch(TransferDataHandler transferDataHandler, PatchId patchId) {
    Patch patch = transferDataHandler.getPatchById(patchId);
    List<Callable<?>> tasks = patch.streamLanesEditable().map(LaneSerializationTask::new).collect(Collectors.toList());

    return ((List<Pair<LaneId, SerializedLane>>) taskExecutorService.executeCallableBatch(tasks)).stream()
        .map(Pair::getRight)
        .collect(Collectors.toList());
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
  public void synchronizedGetIncomingSetsOfCars(TransferDataHandler mapFragment) {
    int countOfNeighbours = mapFragment.getNeighbors().size();
    List<Future<?>> injectIncomingCarFutures = new LinkedList<>();
    int consumedMessages = 0;
    // List<Runnable> injectIncomingCarTasks = new LinkedList<>();

    while (consumedMessages < countOfNeighbours) {
      try {
        CarTransferMessage msg = incomingMessages.take();

        log.debug("Incoming msg size {}", msg.getCars().size());
        List<Future<?>> f = taskExecutorService.executeBatchReturnFutures(
            List.of(new InjectIncomingCarsTask(msg.getCars(), mapFragment)));
        injectIncomingCarFutures.addAll(f);
        // injectIncomingCarTasks.addAll(List.of(new InjectIncomingCarsTask(msg.getCars(), mapFragment)));
        consumedMessages += 1;

      } catch (InterruptedException e) {
        log.error("Exception when waiting for cars: " + e);
        throw new RuntimeException(e);
      }
    }

    taskExecutorService.waitForAllTaskFinished(injectIncomingCarFutures);
    // taskExecutorService.executeBatch(injectIncomingCarTasks);
    incomingMessages.clear();
    futureIncomingMessages.drainTo(incomingMessages);
  }

  private String getWeaitingByMessage() {
    try {
      final Set<MapFragmentId> mapFragmentIds = incomingMessages.stream()
          .map(m -> DebugUtils.getMapFragment().getPatchIdByRoadId(new RoadId(m.getCars().get(0).getRoadId())))
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
  public void notify(Message message) {
    if (message.getMessageType() != MessagesTypeEnum.CarTransferMessage) {
      return;
    }

    CarTransferMessage carTransferMessage = (CarTransferMessage) message;
    if (incomingMessages.contains(carTransferMessage)) {
      futureIncomingMessages.add(carTransferMessage);
    } else {
      incomingMessages.add(carTransferMessage);
      // notifyAll();
    }
  }
}
