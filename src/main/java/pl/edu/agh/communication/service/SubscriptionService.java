package pl.edu.agh.communication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.communication.Subscriber;
import pl.edu.agh.communication.model.MessagesTypeEnum;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final MessageReceiverService receiverService;

    public void subscribe(Subscriber subscriber, MessagesTypeEnum messagesEnum) {
        receiverService.addNewSubscriber(subscriber, messagesEnum);
    }
}
