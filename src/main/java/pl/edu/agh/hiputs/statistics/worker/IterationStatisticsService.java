package pl.edu.agh.hiputs.statistics.worker;

import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import pl.edu.agh.hiputs.statistics.SimulationPoint;
import pl.edu.agh.hiputs.statistics.StageTimeService;
import pl.edu.agh.hiputs.statistics.worker.IterationStatisticsServiceImpl.IterationInfo;

public interface IterationStatisticsService extends StageTimeService {

  void setCarsNumberInStep(long cars);

  void setOutgoingMessagesToServerInStep(int messages);

  void setOutgoingMessagesInStep(int messages);

  void setOutgoingMessagesSize(long size);

  void setMemoryUsage();

  void endSimulationStep();

  void startSimulationStep();

  IterationInfo getIterationInfo(int step);

  List<IterationInfo> getIterationStatistics();

  List<ImmutablePair<Integer, Long>> getAllByType(SimulationPoint point);

}
