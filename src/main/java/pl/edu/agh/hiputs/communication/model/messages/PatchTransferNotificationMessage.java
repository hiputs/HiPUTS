package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchTransferNotificationMessage implements Message {

  private String senderId;
  private List<String> transferredPatchesList;
  private String receiverId;
  private ConnectionDto connectionDto;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.PatchTransferNotificationMessage;
  }
}
