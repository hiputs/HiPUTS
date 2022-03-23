package pl.edu.agh.communication.model.messages;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.communication.model.MessagesTypeEnum;
import pl.edu.agh.communication.model.serializable.SLane;

import java.util.List;

@Builder
@Getter
public class PatchTransferMessage implements Message{

    /**
     * Transferred patch id
     */
    private final String patchId;

    /**
     * All cars from transferred Patch grouped by Line
     */
    private final List<SLane> sLines;

    @Override
    public MessagesTypeEnum getMessageType() {
        return MessagesTypeEnum.PatchTransferMessage;
    }
}