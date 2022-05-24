package pl.edu.agh.hiputs.communication.model.messages;

import lombok.Builder;
import lombok.Data;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

import java.util.List;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.communication.model.serializable.WorkerDataDto;

@Data
@Builder
public class ServerInitializationMessage implements Message {

  /**
   * Worker patchIds list
   */
  private List<String> patchIds;
  /**
   * Neighbouring info
   */
  private List<WorkerDataDto> workerInfo;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.ServerInitializationMessage;
  }
}
