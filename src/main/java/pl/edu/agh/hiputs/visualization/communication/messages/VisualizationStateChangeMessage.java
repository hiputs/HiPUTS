package pl.edu.agh.hiputs.visualization.communication.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;

@Getter
@AllArgsConstructor
public class VisualizationStateChangeMessage implements Message {

  private final proto.model.VisualizationStateChangeMessage visualizationStateChangeMessage;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.VisualizationStateChangeMessage;
  }
}
