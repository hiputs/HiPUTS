package pl.edu.agh.model.id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class LaneId {
    private final String value;

    public LaneId() {
        this(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return "LaneId{" + value + '}';
    }
}
