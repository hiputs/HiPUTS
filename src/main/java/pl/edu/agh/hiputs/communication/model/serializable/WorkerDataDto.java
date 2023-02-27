package pl.edu.agh.hiputs.communication.model.serializable;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkerDataDto implements Serializable {

  /**
   * Shadow patchIds with owner is this worker
   */
  List<String> patchIds;

  /**
   * Connecting parameters with mapFragmentId
   */
  ConnectionDto connectionData;
}
