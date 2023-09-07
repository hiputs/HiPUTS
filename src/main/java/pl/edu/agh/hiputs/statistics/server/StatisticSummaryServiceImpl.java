package pl.edu.agh.hiputs.statistics.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.FinishSimulationStatisticMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.service.server.SubscriptionService;
import pl.edu.agh.hiputs.service.ConfigurationService;
import pl.edu.agh.hiputs.statistics.SimulationPoint;
import pl.edu.agh.hiputs.statistics.worker.IterationStatisticsServiceImpl.IterationInfo;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.LoadBalancingCostStatistic;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.LoadBalancingStatistic;
import pl.edu.agh.hiputs.statistics.worker.SimulationStatisticServiceImpl.MapStatistic;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticSummaryServiceImpl implements StatisticSummaryService, Subscriber {

  public static final String SEPARATOR = ";";
  private static final String END_LINE = "\n";
  private static final String DIR = "statistic";
  private static final String WORKER_COSTS_CSV = "workerCost.csv";
  private static final String CAR_CSV = "car.csv";
  private static final String WORKER_WAITING_TIME_CSV = "workerWaitingTime.csv";
  private static final String WORKER_LOAD_BALANCING_COST_CSV = "workerLoadBalancingTime.csv";
  private static final String PATCH_EXCHANGES_CSV = "patchExchanges.csv";
  private static final String MAP_STATUS_CSV = "mapStatus.csv";
  private static final String SUMMARY_TIMES_CSV = "summaryTimes.csv";
  private static final String ITERATION_TIMES_CSV = "iterationTimes.csv";
  private static final String ITERATION_DATA_CSV = "iterationData.csv";
  private static final String MESSAGES_SIZES_CSV = "messagesSizes.csv";
  private static final String MESSAGES_COUNT_CSV = "messagesCount.csv";
  private final SubscriptionService subscriptionService;

  private final List<FinishSimulationStatisticMessage> repository = new ArrayList<>();
  private final HashMap<SimulationPoint, Long> serverTimeStatisticRepository = new HashMap<>();

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.FinishSimulationStatisticMessage);
    File file = new File(DIR);
    file.mkdir();
  }

  @Override
  public void generateStatisticCSVs() {

    // createCSVTotalCostByWorker();
    createCSVWorkerCosts();
    CreateCSVCarByWorker();
    // createCSVLoadBalancingCostByWorker();
    createCSVPatchExchangesRecords();
    // createCSVMapStatistic();
    createCSVSummaryTimes();
    createCSVIterationTimes();
    createCSVIterationData();

    createCSVMessagesSizes();
    createCSVMessagesCount();
  }

  @Override
  public void startStage(SimulationPoint stage) {
    serverTimeStatisticRepository.put(stage, System.currentTimeMillis());
  }

  @Override
  public void startStage(List<SimulationPoint> stages) {
    long time = System.currentTimeMillis();
    for (SimulationPoint stage : stages) {
      serverTimeStatisticRepository.put(stage, time);
    }
  }

  @Override
  public void endStage(SimulationPoint stage) {
    long startTime = serverTimeStatisticRepository.get(stage);
    serverTimeStatisticRepository.replace(stage, System.currentTimeMillis() - startTime);
  }

  @Override
  public void endStage(List<SimulationPoint> stages) {
    long endTime = System.currentTimeMillis();
    for (SimulationPoint stage : stages) {
      long startTime = serverTimeStatisticRepository.get(stage);
      serverTimeStatisticRepository.replace(stage, endTime - startTime);
    }
  }

  private void createCSVPatchExchangesRecords() {
    String content = repository
        .stream()
        .flatMap(i -> i.getDecisionRepository()
                .stream()
                .filter(r -> r.getSelectedPatch() != null)
            .map(r -> new PatchMigration(r.getStep(), i.getWorkerId(), r.getSelectedNeighbourId(),
                r.getSelectedPatch())))
        .sorted(Comparator.comparingInt(r -> r.step))
        .map(PatchMigration::toString)
        .collect(Collectors.joining());

    save(content, PATCH_EXCHANGES_CSV);
  }

  private void createCSVSummaryTimes() {
    String header = "NAME" + SEPARATOR + "TIME_TYPE" + SEPARATOR + "TIME" + SEPARATOR + END_LINE;
    String serverName = "SERVER";

    String serverContent = serverTimeStatisticRepository.entrySet()
        .stream()
        .map(set -> serverName + SEPARATOR + set.getKey().toString() + SEPARATOR + set.getValue().toString() + SEPARATOR
            + END_LINE)
        .collect(Collectors.joining());

    String workersContent = repository.stream()
        .map(workerData -> workerData.getTimeStatisticRepository()
            .entrySet()
            .stream()
            .map(set -> workerData.getWorkerId() + SEPARATOR + set.getKey().toString() + SEPARATOR + set.getValue()
                .toString() + SEPARATOR + END_LINE)
            .collect(Collectors.joining()))
        .collect(Collectors.joining());

    save(header + serverContent + workersContent, SUMMARY_TIMES_CSV);
  }

  private void createCSVIterationTimes() {
    List<SimulationPoint> points = Arrays.stream(SimulationPoint.values())
        .filter(point -> !point.toString().contains("SERVER") && !point.toString().contains("WORKER"))
        .sorted(Comparator.comparing(Enum::toString))
        .collect(Collectors.toList());

    String header = "NAME" + SEPARATOR + "STEP" + SEPARATOR + points.stream()
        .map(Enum::toString)
        .collect(Collectors.joining(SEPARATOR)) + END_LINE;

    String workersContent = repository.stream()
        .map(workerData -> workerData.getIterationStatisticRepository()
            .stream()
            .map(iterationStats -> (workerData.getWorkerId() + SEPARATOR + iterationStats.getStep() + SEPARATOR
                + getPointsTimes(points, iterationStats.getIterationTimes()) + END_LINE))
            .collect(Collectors.joining()))
        .collect(Collectors.joining());

    save(header + workersContent, ITERATION_TIMES_CSV);
  }

  private void createCSVMessagesSizes() {
    List<MessagesTypeEnum> points = MessagesTypeEnum.getWorkerMessages()
        .stream()
        .sorted(Comparator.comparing(Enum::toString))
        .collect(Collectors.toList());

    String header = "NAME" + SEPARATOR + "STEP" + SEPARATOR + points.stream()
        .map(Enum::toString)
        .collect(Collectors.joining(SEPARATOR)) + END_LINE;

    String workersContent = repository.stream()
        .map(workerData -> workerData.getIterationStatisticRepository()
            .stream()
            .map(iterationStats -> (workerData.getWorkerId() + SEPARATOR + iterationStats.getStep() + SEPARATOR
                + iterationStats.getOutgoingMessagesSize()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(SEPARATOR)) + END_LINE))
            .collect(Collectors.joining()))
        .collect(Collectors.joining());

    save(header + workersContent, MESSAGES_SIZES_CSV);
  }

  private void createCSVMessagesCount() {
    List<MessagesTypeEnum> points = MessagesTypeEnum.getWorkerMessages()
        .stream()
        .sorted(Comparator.comparing(Enum::toString))
        .collect(Collectors.toList());

    String header = "NAME" + SEPARATOR + "STEP" + SEPARATOR + points.stream()
        .map(Enum::toString)
        .collect(Collectors.joining(SEPARATOR)) + END_LINE;

    String workersContent = repository.stream()
        .map(workerData -> workerData.getIterationStatisticRepository()
            .stream()
            .map(iterationStats -> (workerData.getWorkerId() + SEPARATOR + iterationStats.getStep() + SEPARATOR
                + iterationStats.getOutgoingMessages()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(SEPARATOR)) + END_LINE))
            .collect(Collectors.joining()))
        .collect(Collectors.joining());

    save(header + workersContent, MESSAGES_COUNT_CSV);
  }

  private String getPointsTimes(List<SimulationPoint> points, HashMap<SimulationPoint, Long> iterationTimes) {
    return points.stream().map(point -> iterationTimes.get(point).toString()).collect(Collectors.joining(SEPARATOR));
  }

  private void createCSVIterationData() {
    String header =
        "NAME" + SEPARATOR + "STEP" + SEPARATOR + "CARS" + SEPARATOR + "STOPPED_CARS" + SEPARATOR + "SPEED_SUMMARY"
            + SEPARATOR + "MSG_SERVER_SENT" + SEPARATOR + "MSG_SENT" + SEPARATOR + "ALL_MSG_SENT_SIZE" + SEPARATOR
            + "HEAP_MEM_USED" + SEPARATOR + "NO_HEAP_MEM_USED" + SEPARATOR + "HEAP_MEM_MAX" + SEPARATOR
            + "NO_HEAP_MEM_MAX" + SEPARATOR + "INFO" + SEPARATOR + END_LINE;

    String workersContent = repository.stream().map(workerData -> workerData.getIterationStatisticRepository().stream()
            .map(iterationStats -> workerData.getWorkerId() + SEPARATOR + iterationStats.getStep() + SEPARATOR
                + iterationStats.getCarCountAfterStep() + SEPARATOR + iterationStats.getStoppedCars() + SEPARATOR
                + iterationStats.getSpeedSum() + SEPARATOR + iterationStats.getOutgoingMessagesToServer() + SEPARATOR
                + iterationStats.getOutgoingMessages().stream().reduce(Integer::sum).get() + SEPARATOR
                + iterationStats.getOutgoingMessagesSize().stream().reduce(Integer::sum).get() + SEPARATOR
                + iterationStats.getUsedHeapMemory() + SEPARATOR + iterationStats.getUsedNoHeapMemory() + SEPARATOR
                + iterationStats.getMaxHeapMemory() + SEPARATOR + iterationStats.getMaxNoHeapMemory() + SEPARATOR
                + iterationStats.getInfo() + SEPARATOR + END_LINE)
            .collect(Collectors.joining()))
        .collect(Collectors.joining());

    save(header + workersContent, ITERATION_DATA_CSV);
  }

  private void createCSVMapStatistic() {
    String content = repository.stream()
        .flatMap(i -> i.getMapStatisticRepository().stream())
        .sorted(Comparator.comparingInt(MapStatistic::getStep))
        .map(MapStatistic::toString)
        .collect(Collectors.joining());

    save(content, MAP_STATUS_CSV);
  }

  private void createCSVLoadBalancingCostByWorker() {
    List<StringBuffer> lines = createEmptyStringBufferWithHeaders();

    repository.forEach(repo -> {
      int i = 1;
      for (LoadBalancingCostStatistic info : repo.getBalancingCostRepository()) {
        lines.get(i).append(info.getCost());
        lines.get(i).append(SEPARATOR);
        i++;
      }
    });

    save(lines, WORKER_LOAD_BALANCING_COST_CSV);
  }

  private void createCSVWorkerCosts() {
    List<StringBuffer> lines = createEmptyStringBufferWithHeaders();

    repository.forEach(repo -> {
      int i = 1;
      for (LoadBalancingStatistic info : repo.getBalancingStatisticRepository()) {
        lines.get(i).append(info.getTotalCost());
        lines.get(i).append(SEPARATOR);
        i++;
      }
    });

    save(lines, WORKER_COSTS_CSV);
  }

  private void CreateCSVCarByWorker() {
    List<StringBuffer> lines = createEmptyStringBufferWithHeaders();

    repository.forEach(repo -> {
      int j = 1;
      for (IterationInfo info : repo.getIterationStatisticRepository()) {
        lines.get(j).append(info.getCarCountAfterStep());
        lines.get(j).append(SEPARATOR);
        j++;
      }
    });

    save(lines, CAR_CSV);
  }

  private void createCSVWaitingTime() {
    List<StringBuffer> lines = createEmptyStringBufferWithHeaders();

    repository.forEach(repo -> {
      final int[] i = {1};
      repo.getBalancingStatisticRepository().forEach(info -> {
        lines.get(i[0]).append(info.getWaitingTime());
        lines.get(i[0]).append(SEPARATOR);
        i[0]++;
      });
    });

    save(lines, WORKER_WAITING_TIME_CSV);
  }

  private List<StringBuffer> createEmptyStringBufferWithHeaders() {
    int cells = ConfigurationService.getConfiguration().getSimulationStep();
    List<StringBuffer> lines = new ArrayList<>(cells);

    LongStream.range(0, cells + 1).forEach(i -> {
      lines.add(new StringBuffer());
      lines.get((int) i).append(i - 1).append(SEPARATOR);
    });

    lines.get(0).delete(0, 4);
    lines.get(0).append("step");
    lines.get(0).append(SEPARATOR);

    repository.forEach(repo -> {
      lines.get(0).append(repo.getWorkerId());
      lines.get(0).append(SEPARATOR);
    });
    return lines;
  }

  private void save(List<StringBuffer> lines, String filename) {
    File csvOutputFile = new File(DIR + "/" + filename);
    PrintWriter pw;
    try {
      if (ConfigurationService.getConfiguration().isAppendResults() && csvOutputFile.exists()
          && !csvOutputFile.isDirectory()) {
        pw = new PrintWriter(new FileOutputStream(csvOutputFile, true));
      } else {
        pw = new PrintWriter(csvOutputFile);
      }
      // try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
      lines.stream().map(StringBuffer::toString).forEach(pw::println);
      pw.close();
    } catch (Exception e) {
      log.error("Error until save csv file {}", filename, e);
    }
  }

  private void save(String content, String filename) {
    File csvOutputFile = new File(DIR + "/" + filename);
    PrintWriter pw;
    try {
      if (ConfigurationService.getConfiguration().isAppendResults() && csvOutputFile.exists()
          && !csvOutputFile.isDirectory()) {
        pw = new PrintWriter(new FileOutputStream(csvOutputFile, true));
      } else {
        pw = new PrintWriter(csvOutputFile);
      }
      pw.print(content);
      pw.close();
    } catch (Exception e) {
      log.error("Error until save csv file {}", filename, e);
    }
  }

  @Override
  public void notify(Message message) {
    repository.add((FinishSimulationStatisticMessage) message);

  }

  @AllArgsConstructor
  private static class PatchMigration {

    private final int step;
    private final String sourceWorkerId;
    private final String targetWorkerId;
    private final String patchId;

    @Override
    public String toString() {
      return step + SEPARATOR + sourceWorkerId + SEPARATOR + targetWorkerId + SEPARATOR + patchId + "\n";
    }
  }
}
