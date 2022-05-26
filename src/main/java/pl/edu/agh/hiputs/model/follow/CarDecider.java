package pl.edu.agh.hiputs.model.follow;

import pl.edu.agh.hiputs.model.car.CarEnvironment;
import pl.edu.agh.hiputs.model.car.CarProspector;
import pl.edu.agh.hiputs.model.car.CarReadable;
import pl.edu.agh.hiputs.model.map.mapfragment.RoadStructureReader;

public class CarDecider {

  CarProspector prospector = new CarProspector();
  IdmDecider idmDecider = new IdmDecider();
  BasicJunctionDecider junctionDecider = new BasicJunctionDecider();

  public double makeDecision(CarReadable car, RoadStructureReader roadStructureReader){
    CarEnvironment environment = prospector.getPrecedingCarOrCrossroad(car, roadStructureReader);
    if(!environment.getPrecedingCar().isEmpty()){
      return idmDecider.makeDecision(car, environment);
    }
    else{
      return junctionDecider.makeDecision(car, environment, roadStructureReader);
    }
  }

}
