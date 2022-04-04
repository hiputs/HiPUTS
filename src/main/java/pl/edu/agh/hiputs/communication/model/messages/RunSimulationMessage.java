package pl.edu.agh.hiputs.communication.model.messages;

import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

public class RunSimulationMessage implements Message {

    @Override
    public MessagesTypeEnum getMessageType() {
        return MessagesTypeEnum.RunSimulationMessage;
    }
}
