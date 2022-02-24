package pl.edu.agh.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.scheduler.exception.InsufficientSystemResourcesException;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SchedulerService implements TaskExecutorUseCase {

    private ForkJoinPool threadPoolExecutor;
    private static final int RESERVED_THREADS_NUMBER = 1;

    @PostConstruct
    void init() {
        int cores = getFreeCore();

        if (cores <= 0) {
            throw new InsufficientSystemResourcesException("Insufficient number of cores");
        }
        threadPoolExecutor = new ForkJoinPool(cores);
    }

    private int getFreeCore() {
        return Runtime.getRuntime().availableProcessors() - RESERVED_THREADS_NUMBER;
    }

    @Override
    public void executeBatch(Collection<Runnable> tasks) {
        List<Future<?>> futures = tasks
                .stream()
                .map(t -> threadPoolExecutor.submit(t))
                .collect(Collectors.toList());

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
