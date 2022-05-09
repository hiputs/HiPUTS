package pl.edu.agh.hiputs.communication.service.server;

import pl.edu.agh.hiputs.communication.model.messages.Message;

public interface MessagePropagationService {
    void propagateMessage(Message message, String workerId);
}
