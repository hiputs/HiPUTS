package pl.edu.agh.hiputs.statistics.worker;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.statistics.SimulationPoint;

@Service
@RequiredArgsConstructor
public class IterationStatisticsServiceImpl implements IterationStatisticsService {

  @Getter
  private int step = 0;
  private final ConfigurationService configurationService;
  private List<IterationInfo> iterationStatistics;

  @PostConstruct
  private void init() {
    iterationStatistics = new ArrayList<>(ConfigurationService.getConfiguration().getSimulationStep());
  }

  @Override
  public void startStage(SimulationPoint stage) {
    iterationStatistics.get(step).getIterationTimes().put(stage, System.currentTimeMillis());
  }

  @Override
  public void startStage(List<SimulationPoint> stages) {
    long time = System.currentTimeMillis();
    for (SimulationPoint stage : stages) {
      iterationStatistics.get(step).getIterationTimes().put(stage, time);
    }
  }

  @Override
  public void endStage(SimulationPoint stage) {
    long startTime = iterationStatistics.get(step).getIterationTimes().get(stage);
    iterationStatistics.get(step).getIterationTimes().replace(stage, System.currentTimeMillis() - startTime);
  }

  @Override
  public void endStage(List<SimulationPoint> stages) {
    long endTime = System.currentTimeMillis();

    for (SimulationPoint stage : stages) {
      long startTime = iterationStatistics.get(step).getIterationTimes().get(stage);
      iterationStatistics.get(step).getIterationTimes().replace(stage, endTime - startTime);
    }

  }

  @Override
  public void setCarsNumberInStep(long cars) {
    iterationStatistics.get(step).setCarCountAfterStep(cars);
  }

  @Override
  public void setStoppedCars(long cars) {
    iterationStatistics.get(step).setStoppedCars(cars);
  }

  @Override
  public void setSpeedSum(double speed) {
    iterationStatistics.get(step).setSpeedSum(speed);
  }

  @Override
  public void setOutgoingMessagesToServerInStep(int messages) {
    iterationStatistics.get(step).setOutgoingMessagesToServer(messages);
  }

  @Override
  public void setOutgoingMessagesInStep(int messages) {
    iterationStatistics.get(step).setOutgoingMessages(messages);
  }

  @Override
  public void setOutgoingMessagesSize(long size) {
    iterationStatistics.get(step).setOutgoingMessagesSize(size);
  }

  @Override
  public void setMemoryUsage() {
    MemoryMXBean mxbean = ManagementFactory.getMemoryMXBean();
    iterationStatistics.get(step).setUsedHeapMemory(mxbean.getHeapMemoryUsage().getUsed());
    iterationStatistics.get(step).setUsedNoHeapMemory(mxbean.getNonHeapMemoryUsage().getUsed());
    iterationStatistics.get(step).setMaxHeapMemory(mxbean.getHeapMemoryUsage().getMax());
    iterationStatistics.get(step).setMaxNoHeapMemory(mxbean.getNonHeapMemoryUsage().getMax());
  }

  @Override
  public void endSimulationStep() {
    step++;
  }

  @Override
  public void startSimulationStep() {
    iterationStatistics.add(new IterationInfo());
    iterationStatistics.get(step).setStep(step);
  }

  @Override
  public IterationInfo getIterationInfo(int step) {
    return iterationStatistics.get(step);
  }

  @Override
  public List<ImmutablePair<Integer, Long>> getAllByType(SimulationPoint point) {
    List<ImmutablePair<Integer, Long>> list = new LinkedList<>();

    int i = 0;
    for (IterationInfo info : iterationStatistics) {
      ImmutablePair<Integer, Long> timePair = new ImmutablePair<>(i++, info.getIterationTimes()
          .entrySet()
          .stream()
          .filter(o -> o.getKey() == point)
          .findFirst()
          .orElse(new ImmutablePair<>(SimulationPoint.LOAD_BALANCING, -1L))
          .getValue());
      list.add(timePair);
    }
    return list;
  }

  public List<IterationInfo> getIterationStatistics() {
    return iterationStatistics;
  }

  @Getter
  @Setter
  public static class IterationInfo implements Serializable {

    private HashMap<SimulationPoint, Long> iterationTimes = new HashMap<>();
    private long carCountAfterStep;
    private int outgoingMessagesToServer;
    private int outgoingMessages;
    private long outgoingMessagesSize;
    private long usedHeapMemory;
    private long usedNoHeapMemory;
    private long maxHeapMemory;
    private long maxNoHeapMemory;
    private long stoppedCars;
    private double speedSum;
    private int step;

  }

}
