package pl.edu.agh.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.communication.model.MessagesTypeEnum;
import pl.edu.agh.communication.model.serializable.SCar;

import java.util.LinkedList;

@Getter
@AllArgsConstructor
public class CarTransferMessage implements Message{

    private LinkedList<SCar> cars;

    @Override
    public MessagesTypeEnum getMessageType() {
        return MessagesTypeEnum.CarTransferMessage;
    }
}
