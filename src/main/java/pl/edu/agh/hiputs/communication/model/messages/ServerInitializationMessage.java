package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.serializable.WorkerDataDto;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ServerInitializationMessage implements Message {

  /**
   * Worker patchIds list
   */
  List<String> patchIds;
  /**
   * Neighbouring info
   */
  List<WorkerDataDto> workerInfo;

  /**
   * Worker generated more new car then other
   */
  boolean bigWorker;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.ServerInitializationMessage;
  }
}
