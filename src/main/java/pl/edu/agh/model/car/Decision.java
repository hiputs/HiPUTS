package pl.edu.agh.model.car;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class Decision {

    private final double speed;
    private final double acceleration;
    private final LaneLocation location;
    private final int offsetToMoveOnRoute;

}
