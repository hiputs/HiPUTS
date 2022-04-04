package pl.edu.agh.hiputs.model.id;

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

    public boolean isCrossroad() {
        return this.junctionType == JunctionType.CROSSROAD;
    }

    @Override
    public String toString() {
        return "JunctionId{" + value + " type=" + junctionType + '}';
    }
}
