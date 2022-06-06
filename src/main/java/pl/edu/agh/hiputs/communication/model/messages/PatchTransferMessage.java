package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;

@Builder
@Getter
public class PatchTransferMessage implements Message {

  /**
   * Transferred patch id
   */
  private final String patchId;

  /**
   * Connection to other neighbours
   */
  private final List<ConnectionDto> neighbourConnectionMessage;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.PatchTransferMessage;
  }
}
