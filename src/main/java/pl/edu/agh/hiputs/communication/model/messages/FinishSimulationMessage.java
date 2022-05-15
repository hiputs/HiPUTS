package pl.edu.agh.hiputs.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@AllArgsConstructor
@Getter
public class FinishSimulationMessage implements Message {

    private String mapFragmentId;

    @Override
    public MessagesTypeEnum getMessageType() {
        return MessagesTypeEnum.FinishSimulationMessage;
    }
}
