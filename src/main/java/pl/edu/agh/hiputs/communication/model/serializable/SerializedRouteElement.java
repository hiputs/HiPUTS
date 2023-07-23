package pl.edu.agh.hiputs.communication.model.serializable;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SerializedRouteElement implements Serializable {

  private String junctionId;
  private String outgoingLaneId;
  private String junctionType;
}
