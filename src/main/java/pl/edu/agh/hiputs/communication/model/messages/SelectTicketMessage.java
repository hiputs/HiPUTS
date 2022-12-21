package pl.edu.agh.hiputs.communication.model.messages;

import lombok.Value;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Value
public class SelectTicketMessage implements Message{

  String mapFragmentId;
  int ticket;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.SelectTicketMessage;
  }
}
