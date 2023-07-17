package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;

@Builder
@Getter
public class PatchTransferNotificationMessage implements Message {

  private final String senderId;
  private final List<String> transferredPatchesList;
  @Setter
  private String receiverId;
  @Setter
  private ConnectionDto connectionDto;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.PatchTransferNotificationMessage;
  }
}
