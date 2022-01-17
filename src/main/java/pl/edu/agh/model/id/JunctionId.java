package pl.edu.agh.model.id;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class JunctionId {
    private final String id;

    private JunctionType junctionType;

    public JunctionId() {
        this(UUID.randomUUID().toString());
    }
}
