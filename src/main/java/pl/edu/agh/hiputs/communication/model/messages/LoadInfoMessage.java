package pl.edu.agh.hiputs.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

// @Value
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoadInfoMessage implements Message {

  long carCost;
  long time;
  double mapCost;
  String mapFragmentId;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.LoadInfo;
  }
}
