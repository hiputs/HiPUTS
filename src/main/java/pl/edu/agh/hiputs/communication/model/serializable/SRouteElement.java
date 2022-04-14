package pl.edu.agh.hiputs.communication.model.serializable;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SRouteElement implements Serializable {

  private final String junctionId;
  private final String outgoingLaneId;
  private final String junctionType;
}
