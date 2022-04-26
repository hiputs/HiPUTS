package pl.edu.agh.hiputs.communication.model.messages;

import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.serializable.SCar;

@Getter
@AllArgsConstructor
public class CarTransferMessage implements Message {

  private final List<SCar> cars;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.CarTransferMessage;
  }
}
