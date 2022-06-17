package pl.edu.agh.hiputs.communication.model.messages;

import lombok.Value;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Value
public class WorkerConnectionMessage implements Message {

  String address;
  int port;
  String workerId;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.WorkerConnectionMessage;
  }
}
