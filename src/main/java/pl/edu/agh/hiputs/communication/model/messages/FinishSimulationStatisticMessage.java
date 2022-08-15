package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.Value;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.service.worker.SimulationStatisticServiceImpl.DecisionStatistic;
import pl.edu.agh.hiputs.service.worker.SimulationStatisticServiceImpl.LoadBalancingStatistic;

@Value
public class FinishSimulationStatisticMessage implements Message{

  List<LoadBalancingStatistic> balancingCostRepository;
  List<DecisionStatistic> decisionRepository;
  String id;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.FinishSimulationStatisticMessage;
  }
}
