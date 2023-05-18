package pl.edu.agh.hiputs.loadbalancer;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.LoadInfoMessage;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.loadbalancer.model.BalancingMode;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.statistics.SimulationPoint;
import pl.edu.agh.hiputs.statistics.worker.IterationStatisticsServiceImpl;
import pl.edu.agh.hiputs.statistics.worker.IterationStatisticsServiceImpl.IterationInfo;

@Service
@RequiredArgsConstructor
public class LocalLoadMonitorServiceImpl implements LocalLoadMonitorService {

  private static final Set<SimulationPoint> ACTIVE_TIME =
      Set.of(SimulationPoint.FIRST_ITERATION, SimulationPoint.SECOND_ITERATION_UPDATING_CARS,
          SimulationPoint.SYNCHRONIZATION_AREA_SEND_PATCHES);
  private static final Set<SimulationPoint> WAITING_TIME =
      Set.of(SimulationPoint.WAITING_RECEIVING_CARS, SimulationPoint.WAITING_RECEIVING_PATCHES);
  private static final int MOVING_AVERAGE = 50;

  private TransferDataHandler transferDataHandler;
  private final MessageSenderService messageSenderService;
  private final IterationStatisticsServiceImpl iterationStatisticsService;

  @Override
  public void init(TransferDataHandler transferDataHandler) {
    this.transferDataHandler = transferDataHandler;
  }

  @Override
  public LoadBalancingHistoryInfo getMyLastLoad(int step) {
    IterationInfo info = iterationStatisticsService.getIterationInfo(step);
    List<IterationInfo> iterationInfos = iterationStatisticsService.getIterationStatistics();

    double mapCost = iterationInfos.subList(Math.max(0, step - MOVING_AVERAGE), step)
        .stream()
        .mapToDouble(i -> getActiveTime(i) / Math.max(1.0, i.getCarCountAfterStep()))
        .average()
        .orElse(1.0);

    return LoadBalancingHistoryInfo.builder()
        .age(step)
        .carCost(info.getCarCountAfterStep())
        .timeCost(getActiveTime(info))
        .waitingTime(info.getIterationTimes()
            .entrySet()
            .stream()
            .filter(p -> WAITING_TIME.contains(p.getKey()))
            .map(Entry::getValue)
            .reduce(0L, Long::sum))
        .mapCost(mapCost)
        .build();
  }

  private long getActiveTime(IterationInfo info) {
    return info.getIterationTimes()
        .entrySet()
        .stream()
        .filter(p -> ACTIVE_TIME.contains(p.getKey()))
        .map(Entry::getValue)
        .reduce(0L, Long::sum);
  }

  @Override
  public void notifyAboutMyLoad(int step) {
    if (!ConfigurationService.getConfiguration().getBalancingMode().equals(BalancingMode.NONE)) {
      LoadBalancingHistoryInfo info = getMyLastLoad(step);
      messageSenderService.broadcast(new LoadInfoMessage(info.getCarCost(), info.getTimeCost(), info.getMapCost(),
          transferDataHandler.getMe().getId()));
    }
  }
}