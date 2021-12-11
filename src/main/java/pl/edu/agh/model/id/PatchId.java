package pl.edu.agh.model.id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class PatchId {
    private final String id;

    public PatchId() {
        this(UUID.randomUUID().toString());
    }
}
