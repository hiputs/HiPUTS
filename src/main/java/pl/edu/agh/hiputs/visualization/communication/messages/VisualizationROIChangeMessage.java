package pl.edu.agh.hiputs.visualization.communication.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import proto.model.VisualizationStateChangeMessage.ROIRegion;

@Getter
@AllArgsConstructor
public class VisualizationROIChangeMessage implements Message {

  private final ROIRegion roiRegion;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.VisualizationROIChangeMessage;
  }
}
