package pl.edu.agh.hiputs.loadbalancer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.loadbalancer.model.LoadBalancingHistoryInfo;
import pl.edu.agh.hiputs.loadbalancer.model.SimulationPoint;
import pl.edu.agh.hiputs.loadbalancer.utils.CarCounterUtil;
import pl.edu.agh.hiputs.model.map.mapfragment.TransferDataHandler;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Service
@RequiredArgsConstructor
public class LocalLoadStatisticServiceImpl implements LocalLoadStatisticService, MonitorLocalService {

  private final ConfigurationService configurationService;
  private List<IterationInfo> iterationInfo;
  private int step = 0;
  private long tmpTime;
  private TransferDataHandler transferDataHandler;

  @Override
  public void init(TransferDataHandler transferDataHandler){
    iterationInfo = new ArrayList<>((int) configurationService.getConfiguration().getSimulationStep());
    this.transferDataHandler = transferDataHandler;
  }
  @Override
  public LoadBalancingHistoryInfo getMyLastLoad() {
    IterationInfo info = iterationInfo.get(step);

    return LoadBalancingHistoryInfo.builder()
        .age(step)
        .carCost(info.carCountAfterStep)
        .timeCost(info.iterationInfo
            .stream()
            .map(ImmutablePair::getRight)
            .reduce(0L, Long::sum)
        )
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
      info.iterationInfo.add(new ImmutablePair<>(simulationPoint,
          System.currentTimeMillis() - tmpTime));

      if(simulationPoint == SimulationPoint.SECOND_ITERATION){
        info.carCountAfterStep = CarCounterUtil.countAllCars(transferDataHandler);
      }

      tmpTime = System.currentTimeMillis();
  }

  @Override
  public void endSimulationStep() {
    step++;
  }

  static class IterationInfo {
      List<ImmutablePair<SimulationPoint, Long>> iterationInfo = new LinkedList<>();
      long carCountAfterStep;
  }
}
