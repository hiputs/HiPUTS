package pl.edu.agh.hiputs.communication.model.messages;

import pl.edu.agh.hiputs.server.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.server.communication.model.messages.Message;

public class StatisticMessage implements Message {
    @Override
    public MessagesTypeEnum getMessageType() {
        return MessagesTypeEnum.FinishSimulationStatisticMessage;
    }
}
