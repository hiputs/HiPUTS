package pl.edu.agh.communication.model.messages;

import pl.edu.agh.communication.model.MessagesTypeEnum;

public class RunSimulationMessage implements Message {

    @Override
    public MessagesTypeEnum getMessageType() {
        return MessagesTypeEnum.RunSimulationMessage;
    }
}
