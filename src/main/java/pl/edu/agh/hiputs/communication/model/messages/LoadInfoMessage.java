package pl.edu.agh.hiputs.communication.model.messages;

import lombok.Value;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Value
public class LoadInfoMessage implements Message {

  int carCost;
  long time;
  String mapFragmentId;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.LoadInfo;
  }
}
