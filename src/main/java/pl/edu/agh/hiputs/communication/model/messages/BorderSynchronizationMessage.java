package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedLane;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BorderSynchronizationMessage implements Message {

  /**
   * Simulation step identifier
   */
  int simulationStepNo;

  /**
   * Serialized lanes grouped by serialized PatchId where these lanes belong to
   */
  Map<String, List<SerializedLane>> patchContent;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.BorderSynchronizationMessage;
  }

}
