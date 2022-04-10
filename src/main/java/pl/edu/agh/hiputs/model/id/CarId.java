package pl.edu.agh.hiputs.model.id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class CarId {
    private final String value;

    public static CarId random() {
        return new CarId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return "CarId{" + value + '}';
    }
}
