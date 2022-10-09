package pl.edu.agh.hiputs.communication.service.worker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Connection;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferMessage;
import pl.edu.agh.hiputs.communication.model.messages.PatchTransferNotificationMessage;
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

  private final Map<MapFragmentId, Connection> neighbourRepository = new HashMap<>();
  @Getter
  private final Map<MapFragmentId, ConnectionDto> connectionDtoMap = new HashMap<>();
  private final ConfigurationService configurationService;
  private Connection serverConnection;
  private final SubscriptionService subscriptionService;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.ServerInitializationMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferMessage);
    subscriptionService.subscribe(this, MessagesTypeEnum.PatchTransferNotificationMessage);
  }

  /**
   * @param mapFragmentId - mapFragment and unique worker id
   * @param message - message to send
   *
   * @throws IOException <p>Method send message to specific client</p>
   */
  public void send(MapFragmentId mapFragmentId, Message message) throws IOException {
    log.info("Worker send message to: " + mapFragmentId + " message type: " + message.getMessageType());
    neighbourRepository.get(mapFragmentId).send(message);
  }

  public void sendServerMessage(Message message) throws IOException {
    if (serverConnection == null) {
      createServerConnection();
    }
    log.info("Worker send message to: SERVER message type: " + message.getMessageType());
    serverConnection.send(message);
  }

  private void createServerConnection() {
    Configuration configuration = configurationService.getConfiguration();
    ConnectionDto connectionDto = ConnectionDto.builder()
        .port(configuration.getServerPort())
        .address(configuration.getServerAddress())
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
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @Override
  public void notify(Message message) {
    switch (message.getMessageType()){
      case ServerInitializationMessage -> handleWorkerConnectionMessage(message);
      case PatchTransferMessage -> handlePatchTransferMessage(message);
      case PatchTransferNotificationMessage -> handlePatchTransferNotificationMessage(message);
    }

  }

  private void handlePatchTransferNotificationMessage(Message message) {
    PatchTransferNotificationMessage patchTransferNotificationMessage = (PatchTransferNotificationMessage) message;

    if(neighbourRepository.containsKey(new MapFragmentId(patchTransferNotificationMessage.getReceiverId()))){
      return;
    }

    Connection connection = new Connection(patchTransferNotificationMessage.getConnectionDto());
    connectionDtoMap.put(new MapFragmentId(patchTransferNotificationMessage.getReceiverId()), patchTransferNotificationMessage.getConnectionDto());
    neighbourRepository.put(new MapFragmentId(patchTransferNotificationMessage.getReceiverId()), connection);
  }

  private void handlePatchTransferMessage(Message message) {
    PatchTransferMessage workerConnectionMessage = (PatchTransferMessage) message;
    workerConnectionMessage.getNeighbourConnectionMessage()
        .forEach(c -> {
          if(neighbourRepository.containsKey(new MapFragmentId(c.getId()))){
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
