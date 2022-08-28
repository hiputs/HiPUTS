package pl.edu.agh.hiputs.service.server;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import pl.edu.agh.hiputs.communication.Subscriber;
import pl.edu.agh.hiputs.communication.model.MessagesTypeEnum;
import pl.edu.agh.hiputs.communication.model.messages.FinishSimulationStatisticMessage;
import pl.edu.agh.hiputs.communication.model.messages.Message;
import pl.edu.agh.hiputs.communication.service.server.SubscriptionService;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticSummaryServiceImpl implements StatisticSummaryService, Subscriber {

  private static final String SEPARATOR = ", ";

  private static final String DIR = "statistic";
  private static final String WORKER_COSTS_CSV = "workerCost.csv";
  private static final String WORKER_WAITING_TIME_CSV = "workerWaitingTime.csv";
  private static final String WORKER_LOAD_BALANCING_COST_CSV = "workerLoadBalancingTime.csv";
  private static final String PATCH_EXCHANGES_CSV = "patchExchanges.csv";

  private static final String SUMMARY_TXT = "summary.txt";
  private final SubscriptionService subscriptionService;
  private final ConfigurationService configurationService;

  private long startTime;
  private final List<FinishSimulationStatisticMessage> repository = new ArrayList<>();

  @PostConstruct
  void init() {
    subscriptionService.subscribe(this, MessagesTypeEnum.FinishSimulationStatisticMessage);
    File file = new File(DIR);
    file.mkdir();
  }

  @Override
  public void generateStatisticCSVs() {
    createSummary();
    createCSVTotalCostByWorker();
    createCSVWaitingTimeByWorker();
    createCSVLoadBalancingCostByWorker();
    createCSVPatchExchangesRecords();
  }

  @Override
  public void startTiming() {
    startTime = System.currentTimeMillis();
  }

  private void createSummary() {

    final String content = "Simulation time -> " + (System.currentTimeMillis() - startTime) + "ms \n";

    save(content, SUMMARY_TXT);
  }

  private void createCSVPatchExchangesRecords() {
    String content = repository
        .stream()
        .flatMap(i -> i.getDecisionRepository()
            .stream()
                .filter(r -> r.getSelectedPatch() != null)
            .map(r -> new PatchMigration(r.getStep(), i.getId(), r.getSelectedNeighbourId(), r.getSelectedPatch()))
            )
        .sorted(Comparator.comparingInt(r -> r.step))
        .map(PatchMigration::toString)
        .collect(Collectors.joining());

    save(content, PATCH_EXCHANGES_CSV);
  }

  private void createCSVLoadBalancingCostByWorker() {
    List<StringBuffer> lines = createEmptyStringBufferWithHeaders();

    repository.forEach(repo -> {
      final int[] i = {1};
      repo.getBalancingCostRepository().forEach(info -> {
        lines.get(i[0]).append(info.getCost());
        lines.get(i[0]).append(SEPARATOR);
        i[0]++;
      });
    });

    save(lines, WORKER_LOAD_BALANCING_COST_CSV);
  }

  private void createCSVWaitingTimeByWorker() {
    List<StringBuffer> lines = createEmptyStringBufferWithHeaders();

    repository.forEach(repo -> {
      final int[] i = {1};
      repo.getBalancingStatisticRepository().forEach(info -> {
        lines.get(i[0]).append(info.getTotalCost());
        lines.get(i[0]).append(SEPARATOR);
        i[0]++;
      });
    });

    save(lines, WORKER_COSTS_CSV);
  }

  private void createCSVTotalCostByWorker() {
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
    List<StringBuffer> lines = new ArrayList<>((int) configurationService.getConfiguration().getSimulationStep());

    LongStream.range(0, configurationService.getConfiguration().getSimulationStep() + 1)
        .forEach(i -> lines.add(new StringBuffer()));

    repository.forEach(repo -> {
      lines.get(0).append(repo.getId());
      lines.get(0).append(SEPARATOR);
    });

    return lines;
  }

  private void save(List<StringBuffer> lines, String filename) {
    File csvOutputFile = new File(DIR + "/" + filename);
    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
      lines.stream()
          .map(StringBuffer::toString)
          .forEach(pw::println);
      pw.close();
    } catch (Exception e) {
        log.error("Error until save csv file {}", filename, e);
    }
  }

  private void save(String content, String filename) {
    File csvOutputFile = new File(DIR + "/" + filename);
    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
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
    private int step;
    private String sourceWorkerId;
    private String targetWorkerId;
    private String patchId;

    @Override
    public String toString() {
      return step + SEPARATOR + sourceWorkerId + SEPARATOR + targetWorkerId + SEPARATOR + patchId + "\n";
    }
  }
}
