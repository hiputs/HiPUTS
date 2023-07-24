package pl.edu.agh.hiputs.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkerConnectionMessage implements Message {

  @NonNull
  private String address;
  private int port;
  private String workerId;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.WorkerConnectionMessage;
  }
}
