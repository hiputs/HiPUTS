package pl.edu.agh.hiputs.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MapReadyToReadMessage implements Message {

  String mapPackagePath;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.MapReadyToRead;
  }
}
