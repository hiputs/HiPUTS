package pl.edu.agh.hiputs.communication.model.messages;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;

@Builder
@Getter
public class PatchTransferNotificationMessage implements Message {

  private final String senderId;
  private final String receiverId;
  private final String transferPatchId;
  private final ConnectionDto connectionDto;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.PatchTransferMessage;
  }
}
