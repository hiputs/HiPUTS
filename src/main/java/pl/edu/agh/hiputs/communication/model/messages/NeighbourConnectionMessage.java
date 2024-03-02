package pl.edu.agh.hiputs.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NeighbourConnectionMessage implements Message {

  private String address;
  private int port;
  private String id;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.WorkerConnectionMessage;
  }
}
