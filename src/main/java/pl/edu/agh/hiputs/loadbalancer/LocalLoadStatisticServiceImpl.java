package pl.edu.agh.hiputs.loadbalancer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.model.messages.LoadInfoMessage;
import pl.edu.agh.hiputs.communication.service.worker.MessageSenderService;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.loadbalancer.model.SimulationPoint;
import pl.edu.agh.hiputs.loadbalancer.utils.CarCounterUtil;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Service
@RequiredArgsConstructor
public class LocalLoadStatisticServiceImpl implements LocalLoadStatisticService, MonitorLocalService {

  private static final Set<SimulationPoint> ACTIVE_TIME =
      Set.of(SimulationPoint.FIRST_ITERATION, SimulationPoint.SECOND_ITERATION);
  private static final Set<SimulationPoint> WAITING_TIME =
      Set.of(SimulationPoint.WAITING_FOR_FIRST_ITERATION, SimulationPoint.WAITING_FOR_SECOND_ITERATION);
  private final ConfigurationService configurationService;
  private List<IterationInfo> iterationInfo;
  private int step = 0;
  private long tmpTime;
  private TransferDataHandler transferDataHandler;

  private final MessageSenderService messageSenderService;

  @Override
  public void init(TransferDataHandler transferDataHandler) {
    iterationInfo = new ArrayList<>((int) configurationService.getConfiguration().getSimulationStep());
    this.transferDataHandler = transferDataHandler;
  }

  @Override
  public LoadBalancingHistoryInfo getMyLastLoad() {
    IterationInfo info = iterationInfo.get(step);

    return LoadBalancingHistoryInfo.builder()
        .age(step)
        .carCost(info.carCountAfterStep)
        .timeCost(info.iterationInfo.stream()
            .filter(p -> ACTIVE_TIME.contains(p.getLeft()))
            .map(ImmutablePair::getRight)
            .reduce(0L, Long::sum))
        .waitingTime(info.iterationInfo.stream()
            .filter(p -> WAITING_TIME.contains(p.getLeft()))
            .map(ImmutablePair::getRight)
            .reduce(0L, Long::sum))
        .build();
  }

  @Override
  public void startSimulationStep() {
    iterationInfo.add(new IterationInfo());
    tmpTime = System.currentTimeMillis();
  }

  @Override
  public void markPointAsFinish(SimulationPoint simulationPoint) {
    IterationInfo info = iterationInfo.get(step);
    info.iterationInfo.add(new ImmutablePair<>(simulationPoint, System.currentTimeMillis() - tmpTime));

    if (simulationPoint == SimulationPoint.SECOND_ITERATION) {
      info.carCountAfterStep = CarCounterUtil.countAllCars(transferDataHandler);
    }

    tmpTime = System.currentTimeMillis();
  }

  @Override
  public void endSimulationStep() {
    step++;
  }

  @Override
  public void notifyAboutMyLoad() {
    LoadBalancingHistoryInfo info = getMyLastLoad();
    messageSenderService.broadcast(
        new LoadInfoMessage(info.getCarCost(), info.getTimeCost(), transferDataHandler.getMe().getId()));
  }

  static class IterationInfo {

    List<ImmutablePair<SimulationPoint, Long>> iterationInfo = new LinkedList<>();
    long carCountAfterStep;
  }
}
