package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

// @Value
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PatchTransferMessage implements Message {

  List<SerializedPatchTransfer> serializedPatchTransferList;
  String mapFragmentId;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.PatchTransferMessage;
  }
}
