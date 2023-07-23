package pl.edu.agh.hiputs.communication.model.messages;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.hiputs.communication.model.serializable.ConnectionDto;
import pl.edu.agh.hiputs.communication.model.serializable.SerializedLane;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SerializedPatchTransfer implements Serializable {

  /**
   * Transferred patch id
   */
  private String patchId;

  /**
   * Source mapFragmentId
   */
  private String mapFragmentId;

  /**
   * Connection to other neighbours
   */
  private List<ConnectionDto> neighbourConnectionMessage;

  /**
   * Pair of shadow patches and map fragmentId (patch neighbours)
   */
  private List<ImmutablePair<String, String>> patchIdWithMapFragmentId;

  /**
   * Cars from patch - we have patch structure in repository, but we haven't knowledge about current cars position
   */
  private List<SerializedLane> serializedLanes;
}
