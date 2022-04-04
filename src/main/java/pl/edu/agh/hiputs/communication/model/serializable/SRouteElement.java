package pl.edu.agh.hiputs.communication.model.serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class SRouteElement implements Serializable {
    private final String junctionId;
    private final String outgoingLaneId;
    private final String junctionType;
}
