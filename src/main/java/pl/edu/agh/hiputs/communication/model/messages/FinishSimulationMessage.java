package pl.edu.agh.hiputs.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FinishSimulationMessage implements Message {

  String mapFragmentId;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.FinishSimulationMessage;
  }
}
