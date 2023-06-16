package pl.edu.agh.hiputs.service.routegenerator.generator;

import lombok.AllArgsConstructor;
import org.jgrapht.alg.util.Pair;
import org.springframework.stereotype.Component;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.routegenerator.generator.routegenerator.RouteGenerator;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class FileInputGeneratorImpl implements FileInputGenerator {

  RouteGenerator routeGenerator;
  GeneratorCarAmountProvider carAmountProvider;

  @Override
  public List<Pair<RouteWithLocation, Integer>> generateRouteFileInput(Patch patch, int startStep, int endStep, MapFragment mapFragment, MapRepository mapRepository, Boolean fromMapFragment) {
    var patchTotalLaneLength = patch.getLanesLength();
    List<Pair<RouteWithLocation, Integer>> routesWithStep = new ArrayList<>();
    for(int step = startStep; step <= endStep; step ++){
      var carsAmountToGenerate = carAmountProvider.getCarsToGenerateAmountAtStep(step, patchTotalLaneLength);
      List<RouteWithLocation> newRoutes = fromMapFragment ?
        routeGenerator.generateRoutesFromMapFragment(patch, carsAmountToGenerate, mapFragment) :
        routeGenerator.genrateRoutesFromMapRepository(patch, carsAmountToGenerate, mapRepository);
      for(RouteWithLocation route : newRoutes){
        routesWithStep.add(Pair.of(route, step));
      }
    }
    return routesWithStep;
  }
}
