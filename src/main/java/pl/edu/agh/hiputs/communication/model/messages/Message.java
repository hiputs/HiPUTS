package pl.edu.agh.hiputs.communication.model.messages;

import java.io.Serializable;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

public interface Message extends Serializable {

  MessagesTypeEnum getMessageType();
}
