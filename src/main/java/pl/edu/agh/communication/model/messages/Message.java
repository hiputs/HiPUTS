package pl.edu.agh.communication.model.messages;

import pl.edu.agh.communication.model.MessagesTypeEnum;

import java.io.Serializable;

public interface Message extends Serializable {

    MessagesTypeEnum getMessageType();
}
