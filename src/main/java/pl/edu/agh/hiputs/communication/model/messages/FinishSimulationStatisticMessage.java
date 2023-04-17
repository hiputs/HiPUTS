package pl.edu.agh.hiputs.communication.model.messages;

import java.util.List;
import lombok.Value;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.service.worker.SimulationStatisticServiceImpl.DecisionStatistic;
import pl.edu.agh.hiputs.service.worker.SimulationStatisticServiceImpl.LoadBalancingCostStatistic;
import pl.edu.agh.hiputs.service.worker.SimulationStatisticServiceImpl.LoadBalancingStatistic;
import pl.edu.agh.hiputs.service.worker.SimulationStatisticServiceImpl.MapStatistic;

@Value
public class FinishSimulationStatisticMessage implements Message{

  List<LoadBalancingStatistic> balancingStatisticRepository;
  List<DecisionStatistic> decisionRepository;
  List<LoadBalancingCostStatistic> balancingCostRepository;
  List<MapStatistic> mapStatisticRepository;
  String workerId;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.FinishSimulationStatisticMessage;
  }
}
