package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AvailableTicketMessage implements Message{

  String mapFragmentId;
  List<Integer> freeTicket;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.AvailableTicketMessage;
  }
}
