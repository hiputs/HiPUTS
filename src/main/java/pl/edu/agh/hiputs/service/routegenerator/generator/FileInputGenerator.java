package pl.edu.agh.hiputs.service.routegenerator.generator;

import org.jgrapht.alg.util.Pair;
import pl.edu.agh.hiputs.model.car.RouteWithLocation;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.patch.Patch;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.util.List;

public interface FileInputGenerator {

  /**
   * This method generates file with routes for entire simulation for single patch
   * routes will be hold in different files for every patch but in one dictionary
   * This way every 'worker' can generate routes for his patches parallel
   * It will read timeBasedCarGenerationConfig.json to be able to infer how many routes generate
   */
  List<Pair<RouteWithLocation, Integer>> generateRouteFileInput(Patch patch, int startStep, int endStep,
      MapFragment mapFragment, MapRepository mapRepository, Boolean fromMapFragment);

}
