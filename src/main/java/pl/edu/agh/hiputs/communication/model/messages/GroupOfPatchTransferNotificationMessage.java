package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupOfPatchTransferNotificationMessage implements Message {

  private List<PatchTransferNotificationMessage> patchTransferNotificationMessages;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.GroupOfPatchTransferNotificationMessage;
  }

}
