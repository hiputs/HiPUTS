package pl.edu.agh.hiputs.statistics;

import java.util.List;

public interface StageTimeService {

  void startStage(SimulationPoint stage);

  void startStage(List<SimulationPoint> stages);

  void endStage(SimulationPoint stage);

  void endStage(List<SimulationPoint> stages);

}
