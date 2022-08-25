package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedCar;

@Builder
@Getter
public class PatchTransferMessage implements Message {

  /**
   * Transferred patch id
   */
  private final String patchId;

  /**
   * Source mapFragmentId
   */
  private final String mapFragmentId;

  /**
   * Connection to other neighbours
   */
  private final List<ConnectionDto> neighbourConnectionMessage;

  /**
   * Pair of shadow patches and map fragmentId
   */
  private final List<ImmutablePair<String, String>> patchIdWithMapFragmentId;

  /**
   * Car from patch - we have patch structure in repository, but we haven't knowledge about current cars position
   */
  private final List<SerializedCar> cars;


  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.PatchTransferMessage;
  }
}
