package pl.edu.agh.hiputs.statistic;

public interface SimulationStatisticService {

  void saveLoadBalancingCost(long timeInMilis, long cars, double totalCost,int age);

  void saveLoadBalancingDecision(boolean decision, String selectedPatchId, String selectedNeighbourId, double patchCost, int age);


}
