package pl.edu.agh.hiputs.model.id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.actor.RoadStructureEditor;
import pl.edu.agh.hiputs.model.actor.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.JunctionEditable;
import pl.edu.agh.hiputs.model.map.JunctionReadable;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class JunctionId {
    private final String value;

    private final JunctionType junctionType;

    public static JunctionId randomCrossroad() {
        return new JunctionId(UUID.randomUUID().toString(), JunctionType.CROSSROAD);
    }

    public boolean isCrossroad() {
        return this.junctionType == JunctionType.CROSSROAD;
    }

    public JunctionReadable getReadable(RoadStructureReader reader) {
        return reader.getJunctionReadable(this);
    }

    public JunctionEditable getEditable(RoadStructureEditor editor) {
        return editor.getJunctionEditable(this);
    }

    @Override
    public String toString() {
        return "JunctionId{" + value + " type=" + junctionType + '}';
    }
}
