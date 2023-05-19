package pl.edu.agh.hiputs.scheduler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.scheduler.exception.InsufficientSystemResourcesException;
import pl.edu.agh.hiputs.service.ConfigurationService;

@Slf4j
@Service
public class SchedulerService implements TaskExecutorService {

  private ForkJoinPool threadPoolExecutor;

  @PostConstruct
  public void init() {
    int cores = ConfigurationService.getConfiguration().getCoresPerWorkerCount(); //getFreeCores();

    log.info("Cores available in this processor :" + Runtime.getRuntime().availableProcessors()
        + ". Cores used by Scheduler: " + cores);

    if (cores <= 0) {
      throw new InsufficientSystemResourcesException("Insufficient number of cores");
    }
    threadPoolExecutor = new ForkJoinPool(cores);
  }

  private int getFreeCores() {
    return Runtime.getRuntime().availableProcessors();
  }

  @Override
  public void executeBatch(Collection<Runnable> tasks) {
    List<Future<?>> futures = tasks.stream().map(t -> threadPoolExecutor.submit(t)).collect(Collectors.toList());

    waitForAllTaskFinished(futures);
  }

  @Override
  public List<Future<?>> executeBatchReturnFutures(Collection<Runnable> tasks) {
    List<Future<?>> futures = tasks.stream().map(t -> threadPoolExecutor.submit(t)).collect(Collectors.toList());

    return futures;
  }

  @Override
  public List<?> executeCallableBatch(Collection<Callable<?>> tasks) {
    List<Future<?>> futures = tasks.stream().map(t -> threadPoolExecutor.submit(t)).collect(Collectors.toList());

    return waitForAllTaskReturnResult(futures);
  }

  public void waitForAllTaskFinished(List<Future<?>> futures) {
    for (Future<?> future : futures) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        log.error("Error occurred util waiting for task", e);
      }
    }
  }

  private List<?> waitForAllTaskReturnResult(List<Future<?>> futures) {
    return futures.stream().map(future -> {
      try {
        return future.get();
      } catch (InterruptedException | ExecutionException e) {
        log.error("Error occurred util waiting for task", e);
        return null;
      }
    }).collect(Collectors.toList());
  }
}
