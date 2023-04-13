package pl.edu.agh.hiputs.service.routegenerator.generator;

import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.patch.Patch;

import java.util.List;

public interface CarGenerator {

    List<RouteWithLocation> generateRoutes(Patch patch);
}
