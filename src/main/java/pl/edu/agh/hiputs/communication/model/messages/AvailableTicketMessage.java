package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.Value;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Value
public class AvailableTicketMessage implements Message{

  String mapFragmentId;
  List<Integer> freeTicket;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.AvailableTicketMessage;
  }
}
