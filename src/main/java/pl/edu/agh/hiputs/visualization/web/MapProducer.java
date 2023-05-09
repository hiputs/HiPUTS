package pl.edu.agh.hiputs.visualization.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.model.map.roadstructure.JunctionReadable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class MapProducer {

    public String getMapCoordinates(List<Patch> patches) throws IOException {
        return new ObjectMapper().writeValueAsString(new Message("map",getNodes(patches)));
    }
    private List<List<Double>> getNodes(List<Patch> pathes){
        List<JunctionReadable> junctionReadables = pathes.stream().flatMap(Patch::streamJunctionsReadable).toList();
        Optional<Double> minLatitude = junctionReadables.stream().map(JunctionReadable::getLatitude).min(Double::compareTo);
        Optional<Double> maxLatitude = junctionReadables.stream().map(JunctionReadable::getLatitude).max(Double::compareTo);
        Optional<Double> minLongitude = junctionReadables.stream().map(JunctionReadable::getLongitude).min(Double::compareTo);
        Optional<Double> maxLongitude = junctionReadables.stream().map(JunctionReadable::getLongitude).max(Double::compareTo);

        return List.of(List.of(minLatitude.get(),minLongitude.get()),
                List.of(minLatitude.get(),maxLongitude.get()),
                List.of(maxLatitude.get(),minLongitude.get()),
                List.of(maxLatitude.get(),maxLongitude.get()));
    }
}
