package pl.edu.agh.hiputs.communication.service.worker;

import static pl.edu.agh.hiputs.communication.model.MessagesTypeEnum.ServerInitializationMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Connection;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.model.messages.ServerInitializationMessage;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.model.Configuration;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Service
@RequiredArgsConstructor
public class MessageSenderService implements Subscriber {

  private final Map<MapFragmentId, Connection> neighbourRepository = new HashMap<>();
  private final ConfigurationService configurationService;
  private Connection serverConnection;
  private final SubscriptionService subscriptionService;

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, ServerInitializationMessage);
  }

  /**
   * @param neighbourId - unique worker id
   * @param message - message to send
   *
   * @throws IOException <p>Method send message to specific client</p>
   */
  public void send(MapFragmentId mapFragmentId, Message message) throws IOException {
    neighbourRepository.get(mapFragmentId).send(message);
  }

  public void sendServerMessage(Message message) throws IOException {
    if(serverConnection == null){
      createServerConnection();
    }

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
    ServerInitializationMessage workerConnectionMessage = (ServerInitializationMessage) message;
    workerConnectionMessage.getNeighbourConnections().forEach(c -> {
      Connection connection = new Connection(c);
      neighbourRepository.put(new MapFragmentId(c.getId()), connection);
    });
  }
}
