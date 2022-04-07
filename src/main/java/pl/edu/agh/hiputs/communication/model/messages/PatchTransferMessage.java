package pl.edu.agh.hiputs.communication.model.messages;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.serializable.SLane;

import java.util.List;

@Builder
@Getter
public class PatchTransferMessage implements Message {

    /**
     * Transferred patch id
     */
    private final String patchId;

    /**
     * All cars from transferred Patch grouped by Lane
     */
    private final List<SLane> sLanes;

    @Override
    public MessagesTypeEnum getMessageType() {
        return MessagesTypeEnum.PatchTransferMessage;
    }
}
