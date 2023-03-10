package pl.edu.agh.hiputs.loadbalancer;

import pl.edu.agh.hiputs.loadbalancer.model.SimulationPoint;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;

public interface MonitorLocalService {

  void init(TransferDataHandler transferDataHandler);

  void startSimulationStep();

  void markPointAsFinish(SimulationPoint simulationPoint);

  void endSimulationStep();

  void notifyAboutMyLoad();
}
