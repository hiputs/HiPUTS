package pl.edu.agh.hiputs.communication.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;

@AllArgsConstructor
@Getter
public class WorkerConnectionMessage implements Message {

    private String address;
    private int port;
    private String workerId;

    @Override
    public MessagesTypeEnum getMessageType() {
        return MessagesTypeEnum.WorkerConnectionMessage;
    }
}
