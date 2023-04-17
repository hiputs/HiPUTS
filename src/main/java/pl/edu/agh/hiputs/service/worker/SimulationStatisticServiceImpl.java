package pl.edu.agh.hiputs.service.worker;

import static pl.edu.agh.hiputs.loadbalancer.model.SimulationPoint.LOAD_BALANCING;
import static pl.edu.agh.hiputs.service.server.StatisticSummaryServiceImpl.SEPARATOR;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.FinishSimulationStatisticMessage;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.loadbalancer.LocalLoadStatisticService;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationStatisticServiceImpl implements SimulationStatisticService {

  private final ConfigurationService configurationService;

  private final MessageSenderService messageSenderService;

  private final LocalLoadStatisticService loadStatisticService;

  private boolean enableLogs = false;

  private final List<LoadBalancingStatistic> balancingStatisticRepository = new LinkedList<>();
  private final List<DecisionStatistic> decisionRepository = new LinkedList<>();

  private final List<MapStatistic> mapStatisticsRepository = new LinkedList<>();

  @PostConstruct
  void init() {
    enableLogs = configurationService.getConfiguration().isStatisticModeActive();
  }

  @Override
  public void saveLoadBalancingStatistic(long timeInMilis, long cars, double totalCost, int age, long waitingTime) {
    if (!enableLogs) {
      return;
    }

    balancingStatisticRepository.add(LoadBalancingStatistic.builder()
        .step(age)
        .cars(cars)
        .timeInMilis(timeInMilis)
        .totalCost(totalCost)
        .waitingTime(waitingTime)
        .build());
  }

  @Override
  public void saveLoadBalancingDecision(boolean decision, String selectedPatchId, String selectedNeighbourId,
      double patchCost, int age) {
    if (!enableLogs) {
      return;
    }

    decisionRepository.add(DecisionStatistic.builder()
        .step(age)
        .selectedNeighbourId(selectedNeighbourId)
        .patchCost(patchCost)
        .selectedPatch(selectedPatchId)
        .patchCost(patchCost)
        .build());
  }

  private List<LoadBalancingCostStatistic> getLoadBalancingCost() {
    return loadStatisticService.getAllByType(LOAD_BALANCING)
        .stream()
        .map(i -> LoadBalancingCostStatistic.builder()
            .step(i.getLeft())
            .cost(i.getRight())
            .build())
        .toList();
  }

  @Override
  public void sendStatistic(MapFragmentId mapFragmentId) {
    try {
      messageSenderService.sendServerMessage(
          new FinishSimulationStatisticMessage(
              balancingStatisticRepository,
              decisionRepository,
              getLoadBalancingCost(),
              mapStatisticsRepository,
              mapFragmentId.getId()));
    } catch (IOException e) {
      log.error("Error occured wheen send statistic message", e);
    }
  }

  @Override
  public void saveMapStatistic(MapStatistic mapStatistic) {
    mapStatisticsRepository.add(mapStatistic);
  }



  @Builder
  @Value
  public static class LoadBalancingStatistic implements Serializable {

    long timeInMilis;
    long cars; // number of cars in this iteration in worker?
    double totalCost;
    long waitingTime;
    int step;
  }

  @Builder
  @Value
  public static class DecisionStatistic implements Serializable {

    int step;
    String selectedNeighbourId;
    String selectedPatch;
    double patchCost;
  }

  @Builder
  @Value
  public static class LoadBalancingCostStatistic implements Serializable {

    int step;
    long cost;
  }

  @Builder
  @Value
  public static class MapStatistic implements Serializable {
    int step;
    String workerId;
    int localPatches;
    int borderPatches;
    int shadowPatches;
    List<ImmutablePair<String, Integer>> neighbouring;
    List<String> localPatchesIds;

    @Override
    public String toString(){
      return step + SEPARATOR + workerId + SEPARATOR + localPatches + SEPARATOR + borderPatches + SEPARATOR
          + shadowPatches + SEPARATOR + getLocalPatchesIds() + SEPARATOR + getNeighbourString() + "\n";
    }

    private String getNeighbourString() {
      return neighbouring.stream()
          .map(i -> i.getLeft() + ": " + i.getRight())
          .collect(Collectors.joining(" ; "));
    }

    private String getLocalPatchesIds(){
      return String.join(";", localPatchesIds);
    }
  }
}
