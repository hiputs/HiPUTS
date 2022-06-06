package pl.edu.agh.hiputs.communication.service.server;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.service.server.WorkerSynchronisationService;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService, MessagePropagationService {

  private final Map<MessagesTypeEnum, List<Subscriber>> subscriberRepository = new HashMap<>();
  private final WorkerSynchronisationService workerSynchronisationService;

  @PostConstruct
  private void init() {
    Arrays.stream(MessagesTypeEnum.values())
            .forEach(messagesType -> subscriberRepository.put(messagesType, new LinkedList<>()));
  }

  @Override
  public void subscribe(Subscriber subscriber, MessagesTypeEnum messagesEnum) {
    subscriberRepository.get(messagesEnum).add(subscriber);
  }

  @Override
  public void propagateMessage(Message message, String workerId) {
    subscriberRepository.get(message.getMessageType()).forEach(subscriber -> subscriber.notify(message));
    workerSynchronisationService.handleWorker(message.getMessageType(), workerId);
  }
}
