package pl.edu.agh.hiputs.communication.service.worker;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
  private final ConfigurationService configurationService;
  private Connection serverConnection;
  private final WorkerSubscriptionService subscriptionService;
  private AtomicLong sentMessagesSize;
  private AtomicInteger sentServerMessages;
  private AtomicInteger sentMessages;
  private List<String> sentMessagesType;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferNotificationMessage);
    sentMessagesSize = new AtomicLong(0);
    sentServerMessages = new AtomicInteger(0);
    sentMessages = new AtomicInteger(0);
    sentMessagesType = new LinkedList<>();
  }

  /**
   * @param mapFragmentId - mapFragment and unique worker id
   * @param message - message to send
   *
   * @throws IOException <p>Method send message to specific client</p>
   */
  public void send(MapFragmentId mapFragmentId, Message message) throws IOException {
    log.debug("Worker send message to: " + mapFragmentId + " message type: " + message.getMessageType());

    sentMessagesSize.addAndGet(neighbourRepository.get(mapFragmentId).send(message));

    sentMessages.incrementAndGet();
    // sentMessagesType.add(message.getMessageType().name());

  }

  public void sendServerMessage(Message message) throws IOException {
    if (serverConnection == null) {
      createServerConnection();
    }
    log.debug("Worker send message to: SERVER message type: " + message.getMessageType());
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
    }

  }

  public int getSentMessages() {
    return sentMessages.getAndSet(0);
  }

  public String getSentMessageTypes() {
    String tmp = sentMessagesType.toString();
    sentMessagesType.clear();
    return tmp;
  }

  public int getSentServerMessages() {
    return sentServerMessages.getAndSet(0);
  }

  public long getSentMessagesSize() {
    return sentMessagesSize.getAndSet(0);
  }

  private void handlePatchTransferNotificationMessage(Message message) {
    PatchTransferNotificationMessage patchTransferNotificationMessage = (PatchTransferNotificationMessage) message;

    if (neighbourRepository.containsKey(new MapFragmentId(patchTransferNotificationMessage.getReceiverId()))) {
      return;
    }

    Connection connection = new Connection(patchTransferNotificationMessage.getConnectionDto());
    connectionDtoMap.put(new MapFragmentId(patchTransferNotificationMessage.getReceiverId()),
        patchTransferNotificationMessage.getConnectionDto());
    neighbourRepository.put(new MapFragmentId(patchTransferNotificationMessage.getReceiverId()), connection);
  }

  private void handlePatchTransferMessage(Message message) {
    PatchTransferMessage patchTransferMessage = (PatchTransferMessage) message;
    Set<ConnectionDto> workerConnectionMessages = patchTransferMessage
        .getSerializedPatchTransferList()
        .stream()
        .map(SerializedPatchTransfer::getNeighbourConnectionMessage)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

    workerConnectionMessages
        .forEach(c -> {
          if(c == null || neighbourRepository.containsKey(new MapFragmentId(c.getId()))){
            return;
          }
          Connection connection = new Connection(c);
          connectionDtoMap.put(new MapFragmentId(c.getId()), c);
          neighbourRepository.put(new MapFragmentId(c.getId()), connection);
        });
  }

  private void handleWorkerConnectionMessage(Message message){
    ServerInitializationMessage serverInitializationMessage = (ServerInitializationMessage) message;
    serverInitializationMessage.getWorkerInfo()
        .stream()
        .map(WorkerDataDto::getConnectionData)
        .forEach(c -> {
          Connection connection = new Connection(c);
          connectionDtoMap.put(new MapFragmentId(c.getId()), c);
          neighbourRepository.put(new MapFragmentId(c.getId()), connection);
        });
  }

}
