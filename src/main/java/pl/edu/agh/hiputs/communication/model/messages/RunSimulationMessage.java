package pl.edu.agh.hiputs.communication.model.messages;

import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@NoArgsConstructor
public class RunSimulationMessage implements Message {

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.RunSimulationMessage;
  }
}
