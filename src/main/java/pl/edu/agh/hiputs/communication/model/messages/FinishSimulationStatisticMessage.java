package pl.edu.agh.hiputs.communication.model.messages;

import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.statistics.SimulationPoint;
import pl.edu.agh.hiputs.statistics.worker.IterationStatisticsServiceImpl.IterationInfo;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.DecisionStatistic;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.LoadBalancingCostStatistic;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.LoadBalancingStatistic;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.MapStatistic;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FinishSimulationStatisticMessage implements Message {

  List<LoadBalancingStatistic> balancingStatisticRepository;
  List<DecisionStatistic> decisionRepository;
  List<LoadBalancingCostStatistic> balancingCostRepository;
  List<MapStatistic> mapStatisticRepository;
  List<IterationInfo> iterationStatisticRepository;
  HashMap<SimulationPoint, Long> timeStatisticRepository;
  String workerId;

  @Override
  public MessagesTypeEnum getMessageType() {
    return MessagesTypeEnum.FinishSimulationStatisticMessage;
  }
}
