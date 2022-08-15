package pl.edu.agh.hiputs.service.worker.usecase;

public interface SimulationStatisticService {

  void saveLoadBalancingCost(long timeInMilis, long cars, double totalCost,int age, long waitingTime);

  void saveLoadBalancingDecision(boolean decision, String selectedPatchId, String selectedNeighbourId, double patchCost, int age);


}
