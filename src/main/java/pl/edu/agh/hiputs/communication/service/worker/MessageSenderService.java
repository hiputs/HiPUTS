package pl.edu.agh.hiputs.communication.service.worker;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pl.edu.agh.hiputs.HiPUTS;
import pl.edu.agh.hiputs.communication.Connection;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.GroupOfPatchTransferNotificationMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferMessage;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferNotificationMessage;
import pl.edu.agh.hiputs.communication.model.messages.SerializedPatchTransfer;
import pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.communication.model.serializable.WorkerDataDto;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSenderService implements Subscriber {

  private final Map<MapFragmentId, Connection> neighbourRepository = new ConcurrentHashMap<>();
  @Getter
  private final Map<MapFragmentId, ConnectionDto> connectionDtoMap = new HashMap<>();
  private Connection serverConnection;
  private final WorkerSubscriptionService subscriptionService;
  private AtomicLong sentMessagesSize;
  private AtomicInteger sentServerMessages;
  private AtomicInteger sentMessages;
  private Queue<Pair<MessagesTypeEnum, Integer>> sentMessagesTypeDict;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferNotificationMessage);
    sentMessagesSize = new AtomicLong(0);
    sentServerMessages = new AtomicInteger(0);
    sentMessages = new AtomicInteger(0);
    sentMessagesTypeDict = new ConcurrentLinkedQueue<>();
  }

  /**
   * @param mapFragmentId - mapFragment and unique worker id
   * @param message - message to send
   *
   * @throws IOException <p>Method send message to specific client</p>
   */
  public void send(MapFragmentId mapFragmentId, Message message) throws IOException {
    log.debug("Worker send message to: {} message type: {}", mapFragmentId, message.getMessageType());
    log.debug("neigh repo sed {}\n {} {}, {} {} {}", neighbourRepository.keySet().stream().map(a -> a.getId()).toList(),
        neighbourRepository.keySet().stream().map(a -> a.hashCode()).toList(),
        neighbourRepository.keySet().stream().map(a -> Objects.equals(mapFragmentId, a)).toList(), mapFragmentId,
        mapFragmentId.getId(), mapFragmentId.hashCode());

    sentMessagesTypeDict.add(
        new ImmutablePair<>(message.getMessageType(), neighbourRepository.get(mapFragmentId).send(message)));
    sentMessages.incrementAndGet();
  }

  public void sendServerMessage(Message message) throws IOException {
    if (serverConnection == null) {
      createServerConnection();
    }
    log.debug("Worker send message to: SERVER message type: {}", message.getMessageType());
    sentMessagesSize.addAndGet(serverConnection.send(message));
    sentServerMessages.incrementAndGet();
  }

  private void createServerConnection() {
    Configuration configuration = ConfigurationService.getConfiguration();
    String ip = CollectionUtils.isEmpty(HiPUTS.globalInitArgs) ? "127.0.0.1" : HiPUTS.globalInitArgs.get(0);
    log.info("Server address {}:{}", ip, configuration.getServerPort());
    ConnectionDto connectionDto = ConnectionDto.builder()
        .port(configuration.getServerPort())
        .address(ip)
        .id("SERVER")
        .build();
    serverConnection = new Connection(connectionDto);
  }

  /**
   * @param message - message to send
   *
   *     <p>Method send message to all existing worker</p>
   */
  public void broadcast(Message message) {

    neighbourRepository.values().forEach(n -> {
      try {
        n.send(message);
        sentMessages.incrementAndGet();
        // sentMessagesType.add(message.getMessageType().name());
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @Override
  public void notify(Message message) {
    switch (message.getMessageType()) {
      case ServerInitializationMessage -> handleWorkerConnectionMessage(message);
      case PatchTransferMessage -> handlePatchTransferMessage(message);
      case PatchTransferNotificationMessage -> handlePatchTransferNotificationMessage(message);
      case GroupOfPatchTransferNotificationMessage -> handleGroupOfPatchTransferNotificationMessage(message);
    }
  }

  public int getSentMessages() {
    return sentMessages.getAndSet(0);
  }

  public int getSentServerMessages() {
    return sentServerMessages.getAndSet(0);
  }

  public synchronized String getSentMessagesSize() {
    // return sentMessagesSize.getAndSet(0);

    String result = sentMessagesTypeDict.stream()
        .collect(Collectors.groupingBy(Pair::getLeft, Collectors.summingInt(Pair::getRight)))
        .entrySet()
        .stream()
        .sorted(Entry.comparingByKey())
        .map(val -> val.getKey().toString() + ":" + val.getValue().toString() + ",")
        .reduce("", (res, val) -> res + val);
    sentMessagesTypeDict.clear();
    return result;

  }

  private void handlePatchTransferNotificationMessage(Message message) {
    PatchTransferNotificationMessage patchTransferNotificationMessage = (PatchTransferNotificationMessage) message;
    MapFragmentId newMapId = new MapFragmentId(patchTransferNotificationMessage.getReceiverId());

    // long receiverInRepoCount = neighbourRepository.keySet().stream().filter(key -> key.getId().equals
    // (patchTransferNotificationMessage.getReceiverId())).count();
    if (patchTransferNotificationMessage.getReceiverId() == null || neighbourRepository.containsKey(newMapId)) {
      log.debug("receiverId == null or neighbourRepository contains key for {}",
          patchTransferNotificationMessage.getReceiverId());
      return;
    }

    log.debug("neighbourRepository NOT contain key for {}", patchTransferNotificationMessage.getReceiverId());
    Connection connection = new Connection(patchTransferNotificationMessage.getConnectionDto());
    connectionDtoMap.put(newMapId, patchTransferNotificationMessage.getConnectionDto());
    neighbourRepository.put(newMapId, connection);
  }

  private void handleGroupOfPatchTransferNotificationMessage(Message message) {
    GroupOfPatchTransferNotificationMessage groupPatchTransferNotificationMessage =
        (GroupOfPatchTransferNotificationMessage) message;
    if (!groupPatchTransferNotificationMessage.getPatchTransferNotificationMessages().isEmpty()) {
      log.debug("Gather group of notifications which is not empty - processing");
      groupPatchTransferNotificationMessage.getPatchTransferNotificationMessages()
          .forEach(this::handleGroupOfPatchTransferNotificationMessage);
    }
  }

  private void handlePatchTransferMessage(Message message) {
    PatchTransferMessage patchTransferMessage = (PatchTransferMessage) message;
    Set<ConnectionDto> workerConnectionMessages = patchTransferMessage.getSerializedPatchTransferList()
        .stream()
        .map(SerializedPatchTransfer::getNeighbourConnectionMessage)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

    workerConnectionMessages
        .forEach(c -> {
          log.debug("handlePatchMessage from {}", c.getId());
          MapFragmentId newMapId = new MapFragmentId(c.getId());
          // long mapIdInRepoCount = neighbourRepository.keySet().stream().filter(key -> key.getId().equals(c.getId()
          // )).count();
          if (c == null || neighbourRepository.containsKey(newMapId)) {
            return;
          }
          log.debug("Processing patch transfer message - adding new neighbour {}", c.getId());
          Connection connection = new Connection(c);
          connectionDtoMap.put(newMapId, c);
          neighbourRepository.put(newMapId, connection);
        });
  }

  private void handleWorkerConnectionMessage(Message message){
    ServerInitializationMessage serverInitializationMessage = (ServerInitializationMessage) message;
    serverInitializationMessage.getWorkerInfo()
        .stream()
        .map(WorkerDataDto::getConnectionData)
        .forEach(c -> {
          Connection connection = new Connection(c);
          MapFragmentId newMapId = new MapFragmentId(c.getId());
          connectionDtoMap.put(newMapId, c);
          neighbourRepository.put(newMapId, connection);

          // MapFragmentId a = new MapFragmentId(c.getId());
          // log.debug("Add neigh {} {} - {}, {}, {}", newMapId, newMapId.hashCode(), neighbourRepository.keySet(),
          //     neighbourRepository.get(a), a.hashCode());
        });
  }

}
