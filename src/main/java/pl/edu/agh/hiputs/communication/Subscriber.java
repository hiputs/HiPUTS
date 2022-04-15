package pl.edu.agh.hiputs.communication;

import pl.edu.agh.hiputs.communication.model.messages.Message;

public interface Subscriber {

  void notify(Message message);
}
