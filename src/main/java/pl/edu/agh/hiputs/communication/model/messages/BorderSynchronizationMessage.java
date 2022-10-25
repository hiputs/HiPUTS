package pl.edu.agh.hiputs.communication.model.messages;

import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedLane;

@Getter
@AllArgsConstructor
public class BorderSynchronizationMessage implements Message {

  /**
   * Simulation step identifier
   */
  int simulationStepNo;

  /**
   * Serialized lanes grouped by serialized PatchId where these lanes belong to
   */
  Map<String, Set<SerializedLane>> patchContent;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.BorderSynchronizationMessage;
  }

}
