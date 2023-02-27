package pl.edu.agh.hiputs.communication.model.messages;

import lombok.Value;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Value
public class FinishSimulationMessage implements Message {

  String mapFragmentId;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.FinishSimulationMessage;
  }
}
