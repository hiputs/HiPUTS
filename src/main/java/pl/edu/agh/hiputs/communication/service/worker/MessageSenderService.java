package pl.edu.agh.hiputs.communication.service.worker;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private Map<MessagesTypeEnum, Integer> sentMessagesTypeDict;
  private Map<MessagesTypeEnum, Integer> sentMessagesTypeSizesDict;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferNotificationMessage);
    sentMessagesSize = new AtomicLong(0);
    sentServerMessages = new AtomicInteger(0);
    sentMessages = new AtomicInteger(0);
    sentMessagesTypeDict = new ConcurrentHashMap<>();
    sentMessagesTypeSizesDict = new ConcurrentHashMap<>();
    MessagesTypeEnum.getWorkerMessages().forEach(key -> {
      sentMessagesTypeDict.put(key, 0);
      sentMessagesTypeSizesDict.put(key, 0);
    });
  }

  /**
   * @param mapFragmentId - mapFragment and unique worker id
   * @param message - message to send
   *
   * @throws IOException <p>Method send message to specific client</p>
   */
  public void send(MapFragmentId mapFragmentId, Message message) throws IOException {
    log.debug("Worker send message to: {} message type: {}", mapFragmentId, message.getMessageType());

    sentMessagesTypeSizesDict.replace(message.getMessageType(),
        sentMessagesTypeSizesDict.get(message.getMessageType()) + neighbourRepository.get(mapFragmentId).send(message));

    sentMessagesTypeDict.replace(message.getMessageType(), sentMessagesTypeDict.get(message.getMessageType()) + 1);
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
        sentMessagesTypeSizesDict.replace(message.getMessageType(),
            sentMessagesTypeSizesDict.get(message.getMessageType()) + n.send(message));

        sentMessagesTypeDict.replace(message.getMessageType(), sentMessagesTypeDict.get(message.getMessageType()) + 1);

        sentMessages.incrementAndGet();
        // sentMessagesType.add(message.getMessageType().name());
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void broadcast(Message message, Set<MapFragmentId> receivers) {

    receivers.forEach(r -> {
      try {
        sentMessagesTypeSizesDict.replace(message.getMessageType(),
            sentMessagesTypeSizesDict.get(message.getMessageType()) + neighbourRepository.get(r).send(message));

        sentMessagesTypeDict.replace(message.getMessageType(), sentMessagesTypeDict.get(message.getMessageType()) + 1);

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

  public synchronized List<Integer> getSentMessages() {
    List<Integer> result = MessagesTypeEnum.getWorkerMessages()
        .stream()
        .sorted(Comparator.comparing(Enum::toString))
        .map(key -> sentMessagesTypeDict.get(key))
        .toList();

    MessagesTypeEnum.getWorkerMessages().forEach(key -> sentMessagesTypeDict.put(key, 0));

    return result;
    // return sentMessages.getAndSet(0);
  }

  public int getSentServerMessages() {
    return sentServerMessages.getAndSet(0);
  }

  public synchronized List<Integer> getSentMessagesSize() {
    // return sentMessagesSize.getAndSet(0);

    List<Integer> result = MessagesTypeEnum.getWorkerMessages()
        .stream()
        .sorted(Comparator.comparing(Enum::toString))
        .map(key -> sentMessagesTypeSizesDict.get(key))
        .toList();

    MessagesTypeEnum.getWorkerMessages().forEach(key -> sentMessagesTypeSizesDict.put(key, 0));

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

  // public void removeNeighbourConnection(MapFragmentId mapFragmentId){
  //   connectionDtoMap.remove(mapFragmentId);
  //   neighbourRepository.get(mapFragmentId).closeConnection();
  //   neighbourRepository.remove(mapFragmentId);
  // }

}
