package pl.edu.agh.hiputs.statistic;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.service.worker.SubscriptionService;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Service
@RequiredArgsConstructor
public class SimulationStatisticServiceImpl implements SimulationStatisticService, Subscriber{

  private final SubscriptionService subscriptionService;
  private final ConfigurationService configurationService;

  private boolean enableLogs = false;
  private final List<LoadBalancingStatistic> balancingCostRepository = new LinkedList<>();
  private final List<DecisionStatistic> decisionRepository = new LinkedList<>();

  @PostConstruct
  void init(){
    subscriptionService.subscribe(this, MessagesTypeEnum.FinishSimulationMessage);
    enableLogs = configurationService.getConfiguration().isStatisticModeActive();
  }
  @Override
  public void saveLoadBalancingCost(long timeInMilis, long cars, double totalCost, int age) {
    if(!enableLogs){
      return;
    }

    balancingCostRepository.add(LoadBalancingStatistic.builder()
              .age(age)
              .cars(cars)
              .timeInMilis(timeInMilis)
              .totalCost(totalCost)
          .build());
  }

  @Override
  public void saveLoadBalancingDecision(boolean decision, String selectedPatchId, String selectedNeighbourId, double patchCost, int age) {
    if(!enableLogs){
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
  public void notify(Message message) {
    if(!enableLogs){
      return;
    }
  }

  @Builder
  @Value
  private static class LoadBalancingStatistic {
    long timeInMilis;
    long cars;
    double totalCost;
    int age;
  }

  @Builder
  @Value
  private static class DecisionStatistic {
    int age;
    String selectedNeighbourId;
    String selectedPatch;
    double patchCost;
  }
}
