package pl.edu.agh.hiputs.communication.service.server;

import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

public interface SubscriptionService {
    void subscribe(Subscriber subscriber, MessagesTypeEnum messagesEnum);
}
