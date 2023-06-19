package pl.edu.agh.hiputs.communication.model.messages;

import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;

@Builder
@Getter
public class SerializedPatchTransfer implements Serializable {

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
   * Pair of shadow patches and map fragmentId (patch neighbours)
   */
  private final List<ImmutablePair<String, String>> patchIdWithMapFragmentId;

  /**
   * Cars from patch - we have patch structure in repository, but we haven't knowledge about current cars position
   */
  private final List<byte[]> serializedLanes;
}
