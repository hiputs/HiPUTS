package pl.edu.agh.hiputs.communication.model.messages;

import lombok.Builder;
import lombok.Value;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@Value
@Builder
public class MapReadyToReadMessage implements Message {

  String mapPackagePath;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.MapReadyToRead;
  }
}
