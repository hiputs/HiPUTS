package pl.edu.agh.communication;

import pl.edu.agh.communication.model.messages.Message;

public interface Subscriber {

    void notify(Message message);
}
