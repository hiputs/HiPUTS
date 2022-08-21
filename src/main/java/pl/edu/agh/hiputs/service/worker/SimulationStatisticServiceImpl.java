package pl.edu.agh.hiputs.service.worker;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.FinishSimulationStatisticMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.model.id.MapFragmentId;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.service.worker.usecase.SimulationStatisticService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationStatisticServiceImpl implements SimulationStatisticService {

  private final ConfigurationService configurationService;

  private final MessageSenderService messageSenderService;

  private boolean enableLogs = false;

  private final List<LoadBalancingStatistic> balancingCostRepository = new LinkedList<>();
  private final List<DecisionStatistic> decisionRepository = new LinkedList<>();

  @PostConstruct
  void init() {
    enableLogs = configurationService.getConfiguration().isStatisticModeActive();
  }

  @Override
  public void saveLoadBalancingCost(long timeInMilis, long cars, double totalCost, int age, long waitingTime) {
    if (!enableLogs) {
      return;
    }

    balancingCostRepository.add(
        LoadBalancingStatistic.builder()
            .age(age)
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
        .age(age)
        .selectedNeighbourId(selectedNeighbourId)
        .patchCost(patchCost)
        .selectedPatch(selectedPatchId)
        .patchCost(patchCost)
        .build());
  }

  @Override
  public void sendStatistic(MapFragmentId mapFragmentId) {
    try {
      messageSenderService.sendServerMessage(
          new FinishSimulationStatisticMessage(balancingCostRepository, decisionRepository, mapFragmentId.getId()));
    } catch (IOException e) {
      log.error("Error occured wheen send statistic message", e);
    }
  }

  @Builder
  @Value
  public static class LoadBalancingStatistic implements Serializable {

    long timeInMilis;
    long cars;
    double totalCost;
    long waitingTime;
    int age;
  }

  @Builder
  @Value
  public static class DecisionStatistic implements Serializable {

    int age;
    String selectedNeighbourId;
    String selectedPatch;
    double patchCost;
  }
}
