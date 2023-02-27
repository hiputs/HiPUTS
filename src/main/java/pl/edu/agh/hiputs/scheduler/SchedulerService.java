package pl.edu.agh.hiputs.scheduler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.scheduler.exception.InsufficientSystemResourcesException;

@Slf4j
@Service
public class SchedulerService implements TaskExecutorService {

  private ForkJoinPool threadPoolExecutor;

  @PostConstruct
  public void init() {
    int cores = 48; //getFreeCores();

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

  private void waitForAllTaskFinished(List<Future<?>> futures) {
    for (Future<?> future : futures) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        log.error("Error occurred util waiting for task", e);
      }
    }
  }
}
