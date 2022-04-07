package pl.edu.agh.hiputs.model.id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.hiputs.model.actor.RoadStructureEditor;
import pl.edu.agh.hiputs.model.actor.RoadStructureReader;
import pl.edu.agh.hiputs.model.map.LaneReadable;
import pl.edu.agh.hiputs.model.map.LaneEditable;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class LaneId {
    private final String value;
    
    public static LaneId random() {
        return new LaneId(UUID.randomUUID().toString());
    }
    
    public LaneEditable getEditable(RoadStructureEditor editor) {
        return editor.getLaneEditable(this);
    }
    
    public LaneReadable getReadable(RoadStructureReader reader) {
        return reader.getLaneReadable(this);
    }
    
    @Override
    public String toString() {
        return "LaneId{" + value + '}';
    }
}
