package pl.edu.agh.hiputs.model.follow;

import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarProspector;
import pl.edu.agh.hiputs.model.car.CarProspectorImpl;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

public class MainDeciderImpl implements MainDecider {

  private final CarProspector prospector = new CarProspectorImpl();
  private final FunctionalDecider idmDecider = new IdmDecider();
  private final FunctionalDecider junctionDecider = new BasicJunctionDecider();

  public double makeDecision(CarReadable car, RoadStructureReader roadStructureReader){
    CarEnvironment environment = prospector.getPrecedingCarOrCrossroad(car, roadStructureReader);
    if(environment.getPrecedingCar().isPresent()){
      return idmDecider.makeDecision(car, environment, roadStructureReader);
    }
    else{
      return junctionDecider.makeDecision(car, environment, roadStructureReader);
    }
  }

}
