package pl.edu.agh.model.id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class JunctionId {
    private final String id;

    private final JunctionType junctionType;

    public JunctionId() {
        this(UUID.randomUUID().toString(), JunctionType.CROSSROAD);
    }
}
