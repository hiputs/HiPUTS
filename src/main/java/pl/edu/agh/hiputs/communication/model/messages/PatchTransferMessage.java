package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.Value;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Value
public class PatchTransferMessage implements Message{

  List<SerializedPatchTransfer> serializedPatchTransferList;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.PatchTransferMessage;
  }
}
