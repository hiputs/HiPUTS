package pl.edu.agh.hiputs.service.routegenerator;

import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.patch.Patch;

import java.util.List;

public interface CarGeneratorService {

    public void generateRoutes(Patch patch);

}
