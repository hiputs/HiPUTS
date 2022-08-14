package pl.edu.agh.hiputs.communication.model.messages;

import java.util.LinkedList;
import java.util.List;
import lombok.Value;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.statistic.SimulationStatisticServiceImpl.DecisionStatistic;
import pl.edu.agh.hiputs.statistic.SimulationStatisticServiceImpl.LoadBalancingStatistic;

@Value
public class FinishSimulationStatisticMessage implements Message{

  List<LoadBalancingStatistic> balancingCostRepository;
  List<DecisionStatistic> decisionRepository;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.FinishSimulationStatisticMessage;
  }
}
