package pl.edu.agh.hiputs.visualization.communication.messages;

import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;

public class StopSimulationMessage implements Message {

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.StopSimulationMessage;
  }
}
