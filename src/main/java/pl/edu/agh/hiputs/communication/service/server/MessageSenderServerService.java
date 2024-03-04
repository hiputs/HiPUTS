package pl.edu.agh.hiputs.communication.service.server;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.Message;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSenderServerService {

  private final WorkerRepository workerRepository;

  /**
   * @param workerId - unique worker id
   * @param message - message to send
   *
   * @throws IOException <p>Method send message to specific client</p>
   */
  public void send(String workerId, Message message) {
    workerRepository.get(workerId).send(message);
  }

  /**
   * @param message - message to send
   *
   *     <p>Method send message to all existing worker</p>
   */
  public void broadcast(Message message) {
    log.info("Broadcasting message {}", message.getMessageType());
    workerRepository.getAll().forEach(n -> n.send(message));
  }
}
