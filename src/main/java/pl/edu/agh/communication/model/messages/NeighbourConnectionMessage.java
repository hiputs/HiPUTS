package pl.edu.agh.communication.model.messages;

import pl.edu.agh.communication.model.MessagesTypeEnum;
import lombok.Builder;
import lombok.Data;

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
