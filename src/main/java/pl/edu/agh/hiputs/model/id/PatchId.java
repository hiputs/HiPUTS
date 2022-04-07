package pl.edu.agh.hiputs.model.id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class PatchId {
    private final String value;
    
    public static PatchId random() {
        return new PatchId(UUID.randomUUID().toString());
    }
}
