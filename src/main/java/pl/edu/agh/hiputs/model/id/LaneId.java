package pl.edu.agh.hiputs.model.id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.actor.RoadStructureEditor;
import pl.edu.agh.hiputs.model.actor.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.LaneRead;
import pl.edu.agh.hiputs.model.map.LaneReadWrite;

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

    public LaneReadWrite getLaneReadWrite(RoadStructureEditor editor) {
        return editor.getLaneReadWriteById(this);
    }

    public LaneRead getLaneRead(RoadStructureReader reader) {
        return reader.getLaneReadById(this);
    }
}
