package pl.edu.agh.hiputs.service.routegenerator.generator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator.RouteGenerator;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.sql.Timestamp;
import java.util.List;

@Component
@AllArgsConstructor
public class FileInputGeneratorImpl implements FileInputGenerator {

  RouteGenerator routeGenerator;

  @Override
  public List<RouteWithLocation> generateRouteFileInput(Patch patch, Timestamp startTime, Timestamp endTime, MapFragment mapFragment) {
//    TODO: na razie generujemy losowe trasy do pliku
    return routeGenerator.generateRoutes(patch, 5, mapFragment);
  }
}
