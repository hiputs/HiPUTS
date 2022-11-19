package pl.edu.agh.hiputs.communication.service.worker;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Service
@RequiredArgsConstructor
public class WorkerSubscriptionService {

  private final MessageReceiverService receiverService;

  public void subscribe(Subscriber subscriber, MessagesTypeEnum messagesEnum) {
    receiverService.addNewSubscriber(subscriber, messagesEnum);
  }
}
