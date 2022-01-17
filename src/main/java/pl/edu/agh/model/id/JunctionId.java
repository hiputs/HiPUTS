package pl.edu.agh.model.id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class JunctionId {
    private final String value;

    private final JunctionType junctionType;

    public JunctionId() {
        this(UUID.randomUUID().toString(), JunctionType.CROSSROAD);
    }

    public boolean isCrossroad(){ // TODO #15 add method or anyway to check junction type
        throw new UnsupportedOperationException("method not implemented!");
    }
}
