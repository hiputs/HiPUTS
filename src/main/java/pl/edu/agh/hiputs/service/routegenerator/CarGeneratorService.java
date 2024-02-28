package pl.edu.agh.hiputs.service.routegenerator;

import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;

public interface CarGeneratorService {

  void manageCars(MapFragment fragment, int step);

  void generateInitialCars(MapFragment mapFragment);

}

