package pl.edu.agh.hiputs.service.worker.usecase;

import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.service.worker.SimulationStatisticServiceImpl.MapStatistic;

public interface SimulationStatisticService {

  void saveLoadBalancingStatistic(long timeInMilis, long cars, double totalCost,int age, long waitingTime);

  void saveLoadBalancingDecision(boolean decision, String selectedPatchId, String selectedNeighbourId, double patchCost, int age);

  void sendStatistic(MapFragmentId mapFragmentId);

  void saveMapStatistic(MapStatistic mapStatistic);

}
