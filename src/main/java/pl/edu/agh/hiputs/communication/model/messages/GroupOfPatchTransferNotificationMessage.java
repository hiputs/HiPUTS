package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Builder
@Getter
public class GroupOfPatchTransferNotificationMessage implements Message {

  private final List<PatchTransferNotificationMessage> patchTransferNotificationMessages;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.GroupOfPatchTransferNotificationMessage;
  }

}
