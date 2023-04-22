package pl.edu.agh.hiputs.service.routegenerator;

import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;

public interface CarGeneratorService {

    void generateCars(MapFragment fragment, int step);

}
