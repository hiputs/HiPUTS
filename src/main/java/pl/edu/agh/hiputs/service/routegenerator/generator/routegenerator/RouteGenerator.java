package pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator;

import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.List;

public interface RouteGenerator {
  List<RouteWithLocation> generateRoutes(MapRepository mapRepository, Patch patch, int numberOfRoutes);
}
