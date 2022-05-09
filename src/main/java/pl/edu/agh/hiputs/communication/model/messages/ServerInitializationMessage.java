package pl.edu.agh.hiputs.communication.model.messages;

import lombok.Builder;
import lombok.Data;
import pl.edu.agh.hiputs.server.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.server.communication.model.messages.Message;
import pl.edu.agh.hiputs.server.communication.model.serializable.SNeighbourConnection;
import pl.edu.agh.hiputs.server.communication.model.serializable.SPath;

import java.util.List;

@Data
@Builder
public class ServerInitializationMessage implements Message {

  private List<SPath> patches;
  private List<SNeighbourConnection> neighbourConnections;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.ServerInitializationMessage;
  }
}
