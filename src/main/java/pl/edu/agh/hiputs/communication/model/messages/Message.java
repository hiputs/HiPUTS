package pl.edu.agh.hiputs.communication.model.messages;

import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

import java.io.Serializable;

public interface Message extends Serializable {

    MessagesTypeEnum getMessageType();
}
