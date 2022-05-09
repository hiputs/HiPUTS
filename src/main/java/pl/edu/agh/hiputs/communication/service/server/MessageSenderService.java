package pl.edu.agh.hiputs.communication.service.server;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.Message;

@Service
@RequiredArgsConstructor
public class MessageSenderService {

  private final WorkerRepository workerRepository;

  /**
   * @param neighbourId - unique worker id
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
    workerRepository.getAll()
            .forEach(n -> n.send(message));
  }
}
