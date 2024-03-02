package pl.edu.agh.hiputs.service.worker.strategies;

import pl.edu.agh.hiputs.partition.model.lights.control.SignalsControlCenter;

public interface TrafficLightsStrategy {

  void execute(SignalsControlCenter signalsControlCenter);
}
