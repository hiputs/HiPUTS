package pl.edu.agh.communication.model.messages;

import lombok.Builder;
import lombok.Data;
import pl.edu.agh.communication.model.MessagesTypeEnum;

@Data
@Builder
public class NeighbourConnectionMessage implements Message {

    private String address;
    private int port;
    private String id;

    @Override
    public MessagesTypeEnum getMessageType() {
        return MessagesTypeEnum.WorkerConnectionMessage;
    }
}
